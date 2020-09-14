package services.api.impl;

import com.fasterxml.jackson.databind.JsonNode;
import criterias.LogEntryCriteria;
import criterias.UserCriteria;
import dto.DtoPagedList;
import dto.NotFoundException;
import dto.Profile;
import io.ebean.PagedList;
import mappers.AggregateMapper;
import mappers.ProfileMapper;
import mappers.UserMapper;
import mappers.UserObfuscatorMapper;
import models.LinkedAccount;
import models.Scope;
import models.User;
import play.libs.Json;
import play.mvc.Http;
import services.AuthProvider;
import services.LogEntryService;
import services.PermissionService;
import services.UserService;
import services.api.UserApiService;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

/**
 * @author resamsel
 * @version 29 Jan 2017
 */
@Singleton
public class UserApiServiceImpl extends
        AbstractApiService<User, UUID, UserCriteria, UserService, dto.User>
        implements UserApiService {

  private final AuthProvider authProvider;
  private final LogEntryService logEntryService;

  @Inject
  protected UserApiServiceImpl(
          UserService userService,
          AuthProvider authProvider,
          PermissionService permissionService,
          LogEntryService logEntryService, Validator validator) {
    super(userService, dto.User.class, UserMapper::toDto,
            new Scope[]{Scope.UserRead},
            new Scope[]{Scope.UserWrite},
            permissionService,
            validator
    );

    this.authProvider = authProvider;
    this.logEntryService = logEntryService;
  }

  @Override
  protected Function<User, dto.User> getDtoMapper() {
    return super.getDtoMapper()
            .andThen(UserObfuscatorMapper.of(authProvider.loggedInUser()));
  }

  @Override
  public dto.User create(Http.Request request) {
    User user = toModel(toDto(request.body().asJson()));

    authProvider.loggedInProfile(request).ifPresent(profile -> {
      LinkedAccount linkedAccount = new LinkedAccount().withUser(user);
      linkedAccount.providerKey = profile.getClientName();
      linkedAccount.providerUserId = profile.getId();
      user.linkedAccounts = Collections.singletonList(linkedAccount);
    });

    return getDtoMapper().apply(service.create(user));
  }

  @Override
  public dto.User byUsername(String username, String... propertiesToFetch) {
    permissionService.checkPermissionAll("Access token not allowed", readScopes);

    return Optional.ofNullable(service.byUsername(username, propertiesToFetch))
            .map(getDtoMapper())
            .orElseThrow(() -> new NotFoundException(dto.User.class.getSimpleName(), username));
  }

  @Override
  public PagedList<dto.Aggregate> activity(UUID id) {
    permissionService.checkPermissionAll("Access token not allowed", readScopes);

    return new DtoPagedList<>(
            logEntryService.getAggregates(new LogEntryCriteria().withUserId(id)),
            AggregateMapper::toDto);
  }

  @Override
  public Profile profile(Http.Request request) {
    return authProvider.loggedInProfile(request)
            .map(ProfileMapper::toDto)
            .orElse(null);
  }

  @Override
  public dto.User me(Http.Request request, String... propertiesToFetch) {
    UUID loggedInUserId = authProvider.loggedInUserId(request);

    return dtoMapper.apply(service.byId(loggedInUserId, propertiesToFetch));
  }

  @Override
  public dto.User saveSettings(UUID userId, JsonNode json) {
    return dtoMapper.apply(service.saveSettings(userId, Json.fromJson(json, Map.class)));
  }

  @Override
  public dto.User updateSettings(UUID userId, JsonNode json) {
    return dtoMapper.apply(service.updateSettings(userId, Json.fromJson(json, Map.class)));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected User toModel(dto.User in) {
    return UserMapper.toModel(in, service.byId(in.id));
  }
}
