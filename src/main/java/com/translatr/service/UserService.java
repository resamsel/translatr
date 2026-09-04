package com.translatr.service;

import com.translatr.config.TranslatrConfig;
import com.translatr.criteria.UserCriteria;
import com.translatr.dto.PagedList;
import com.translatr.dto.UserDto;
import com.translatr.mapper.DtoMapper;
import com.translatr.model.ActionType;
import com.translatr.model.LinkedAccount;
import com.translatr.model.User;
import com.translatr.model.UserRole;
import com.translatr.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.eclipse.microprofile.jwt.JsonWebToken;

@ApplicationScoped
public class UserService {

    private final UserRepository   userRepo;
    private final FeatureResolver  featureResolver;
    private final DtoMapper        mapper;
    private final ActivityLogger   activity;
    private final TranslatrConfig  config;

    @Inject
    public UserService(UserRepository userRepo, FeatureResolver featureResolver, DtoMapper mapper,
                       ActivityLogger activity, TranslatrConfig config) {
        this.userRepo        = userRepo;
        this.featureResolver = featureResolver;
        this.mapper          = mapper;
        this.activity        = activity;
        this.config          = config;
    }

    private static final List<String> ORDERABLE =
        List.of("username", "name", "email", "whenCreated", "whenUpdated");

    public PagedList<UserDto> find(UserCriteria c) {
        // Port of UserRepositoryImpl.findBy: honour username / email / search / order, not findAll().
        StringBuilder ql    = new StringBuilder("FROM User u WHERE 1 = 1");
        List<Object>  params = new ArrayList<>();

        if (QuerySupport.hasText(c.username)) {
            params.add(c.username);
            ql.append(" AND u.username = ?").append(params.size());
        }
        if (QuerySupport.hasText(c.email)) {
            params.add(c.email);
            ql.append(" AND u.email = ?").append(params.size());
        }
        if (QuerySupport.hasText(c.search)) {
            params.add(QuerySupport.like(c.search));
            int i = params.size();
            ql.append(" AND (lower(u.name) LIKE ?").append(i)
              .append(" OR lower(u.username) LIKE ?").append(i).append(')');
        }
        ql.append(' ').append(QuerySupport.orderBy(c.order, ORDERABLE, "ORDER BY username"));

        var query  = userRepo.find(ql.toString(), params.toArray());
        long total = query.count();
        var list   = query.page(c.offset / Math.max(c.limit,1), c.limit).list()
                          .stream().map(mapper::toDto).collect(Collectors.toList());
        return new PagedList<>(list, total, c.offset, c.limit);
    }

    public UserDto get(UUID id) {
        UserDto dto = mapper.toDto(userRepo.findByIdOptional(id).orElseThrow(NotFoundException::new));
        attachFeatures(dto);
        return dto;
    }

    public UserDto getByUsername(String username) {
        UserDto dto = mapper.toDto(userRepo.findByUsername(username).orElseThrow(NotFoundException::new));
        attachFeatures(dto);
        return dto;
    }

    // UserDto.features is the current user's effective feature map (override → global → default),
    // resolved by FeatureResolver so it always covers every known Feature.
    private void attachFeatures(UserDto dto) {
        dto.features = featureResolver.resolveAll(dto.id);
    }

    @Transactional
    public UserDto update(UserDto dto) {
        User u = userRepo.findByIdOptional(dto.id).orElseThrow(NotFoundException::new);
        UserDto before = mapper.toDto(u);
        if (dto.username        != null) u.username        = dto.username;
        if (dto.name            != null) u.name            = dto.name;
        if (dto.email           != null) u.email           = dto.email;
        if (dto.preferredLocale != null) u.preferredLocale = dto.preferredLocale;
        if (dto.settings != null) {
            if (u.settings == null) u.settings = new java.util.HashMap<>();
            u.settings.putAll(dto.settings);
        }
        UserDto after = mapper.toDto(u);
        activity.publish(ActionType.Update, null, UserDto.class, before, after);
        return after;
    }

    @Transactional
    public UserDto delete(UUID id) {
        User u = userRepo.findByIdOptional(id).orElseThrow(NotFoundException::new);
        userRepo.delete(u);
        return mapper.toDto(u);
    }

    @Transactional
    public User findOrCreate(String providerKey, String providerUserId, String name, String email) {
        // 1. Fast path: look up via linked account (normal case after first login)
        var byLinkedAccount = userRepo.findByLinkedAccount(providerKey, providerUserId);
        if (byLinkedAccount.isPresent()) {
            return byLinkedAccount.get();
        }

        // 2. Fallback: a user with the same derived username already exists (e.g. from a
        //    prior run that crashed before the LinkedAccount was committed). Re-use that
        //    user and add the missing linked account so future lookups take the fast path.
        String derivedUsername = email != null
                ? email.replaceAll("[^a-zA-Z0-9_.-]", "")
                : providerUserId;
        User u = userRepo.findByUsername(derivedUsername).orElseGet(() -> {
            User nu = new User();
            nu.name     = name;
            nu.email    = email;
            nu.username = derivedUsername;
            userRepo.persist(nu);
            return nu;
        });

        // We know no LinkedAccount exists for this provider/userId — persist one now.
        LinkedAccount la = new LinkedAccount();
        la.user           = u;
        la.providerKey    = providerKey;
        la.providerUserId = providerUserId;
        la.persist();

        return u;
    }

    /**
     * Reconciles a user's {@link UserRole} with their identity on every OIDC login:
     * holding {@link TranslatrConfig#adminGroup()} in the {@code groups} claim, OR having
     * an {@code email} claim listed in {@link TranslatrConfig#adminEmails()} ({@code ADMINS}),
     * grants {@code Admin}; losing both demotes back to {@code User}. {@code groups} is the
     * OIDC {@code groups} claim; Keycloak's "full group path" form ({@code /translatr-admin})
     * is accepted too. The {@code ADMINS} e-mail path applies to every provider — social
     * logins carry no {@code groups} claim, so it is their only route to Admin. Only the OIDC
     * login path calls this — access-token identities keep their stored role.
     */
    @Transactional
    public User syncOidcRole(UUID userId, JsonWebToken jwt) {
        User user = userRepo.findById(userId);
        if (user == null) {
            return null;
        }
        String email = jwt.getClaim("email");
        boolean admin = inAdminGroup(jwt.getGroups())
                || (email != null && config.adminEmails().contains(email.toLowerCase(Locale.ROOT)));
        UserRole desired = admin ? UserRole.Admin : UserRole.User;
        if (user.role != desired) {
            user.role = desired;
        }
        return user;
    }

    private boolean inAdminGroup(Set<String> groups) {
        if (groups == null || groups.isEmpty()) {
            return false;
        }
        String adminGroup = config.adminGroup();
        return groups.stream()
                .map(g -> g.startsWith("/") ? g.substring(1) : g)
                .anyMatch(g -> g.equalsIgnoreCase(adminGroup));
    }
}
