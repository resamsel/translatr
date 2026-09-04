package com.translatr.service;

import com.translatr.config.TranslatrConfig;
import com.translatr.criteria.UserCriteria;
import com.translatr.dto.UserDto;
import com.translatr.mapper.DtoMapper;
import com.translatr.model.ActionType;
import com.translatr.model.User;
import com.translatr.model.UserRole;
import com.translatr.repository.UserRepository;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository            userRepo;
    @Mock FeatureResolver           featureResolver;
    @Mock DtoMapper                 mapper;
    @Mock ActivityLogger            activity;
    @Mock TranslatrConfig          config;

    @InjectMocks UserService service;

    @SuppressWarnings("unchecked")
    private String runFindAndCaptureQuery(UserCriteria c) {
        PanacheQuery<User> query = mock(PanacheQuery.class);
        when(userRepo.find(anyString(), any(Object[].class))).thenReturn(query);
        when(query.count()).thenReturn(0L);
        when(query.page(anyInt(), anyInt())).thenReturn(query);
        when(query.list()).thenReturn(List.of());

        service.find(c);

        ArgumentCaptor<String> ql = ArgumentCaptor.forClass(String.class);
        verify(userRepo).find(ql.capture(), any(Object[].class));
        return ql.getValue();
    }

    @Test
    void find_translatesUsernameEmailSearchAndOrder() {
        UserCriteria c = new UserCriteria();
        c.username = "jane";
        c.email    = "jane@example.com";
        c.search   = "jan";
        c.order    = "username asc";
        c.limit    = 20;

        String ql = runFindAndCaptureQuery(c);
        assertThat(ql).contains("u.username = ");
        assertThat(ql).contains("u.email = ");
        assertThat(ql).contains("lower(u.username) LIKE ");
        assertThat(ql).endsWith("ORDER BY username ASC");
    }

    // -------------------------------------------------------------------------
    // get
    // -------------------------------------------------------------------------

    @Test
    void get_throwsNotFound_whenUserMissing() {
        UUID id = UUID.randomUUID();
        when(userRepo.findByIdOptional(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(id))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void get_returnsDto_whenUserExists() {
        UUID id   = UUID.randomUUID();
        User user = userWithId(id);
        UserDto dto = new UserDto();
        dto.id = id;

        when(userRepo.findByIdOptional(id)).thenReturn(Optional.of(user));
        when(mapper.toDto(user)).thenReturn(dto);
        when(featureResolver.resolveAll(id)).thenReturn(java.util.Map.of("language-switcher", false));

        UserDto result = service.get(id);

        assertThat(result.id).isEqualTo(id);
    }

    @Test
    void get_attachesResolvedFeatures() {
        UUID id   = UUID.randomUUID();
        User user = userWithId(id);
        UserDto dto = new UserDto();
        dto.id = id;

        when(userRepo.findByIdOptional(id)).thenReturn(Optional.of(user));
        when(mapper.toDto(user)).thenReturn(dto);
        when(featureResolver.resolveAll(id))
            .thenReturn(java.util.Map.of("header-graphic", true, "language-switcher", false));

        UserDto result = service.get(id);

        assertThat(result.features).containsEntry("header-graphic", true);
        assertThat(result.features).containsEntry("language-switcher", false);
    }

    // -------------------------------------------------------------------------
    // getByUsername
    // -------------------------------------------------------------------------

    @Test
    void getByUsername_throwsNotFound_whenUserMissing() {
        when(userRepo.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getByUsername("ghost"))
                .isInstanceOf(NotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // update
    // -------------------------------------------------------------------------

    @Test
    void update_appliesAllMutableFields() {
        UUID id   = UUID.randomUUID();
        User user = userWithId(id);

        UserDto dto = new UserDto();
        dto.id             = id;
        dto.username       = "newuser";
        dto.name           = "New Name";
        dto.email          = "new@example.com";
        dto.preferredLocale = "de";
        dto.settings       = Map.of("theme", "dark");

        when(userRepo.findByIdOptional(id)).thenReturn(Optional.of(user));
        when(mapper.toDto(user)).thenReturn(dto);

        service.update(dto);

        assertThat(user.username).isEqualTo("newuser");
        assertThat(user.name).isEqualTo("New Name");
        assertThat(user.email).isEqualTo("new@example.com");
        assertThat(user.preferredLocale).isEqualTo("de");
        assertThat(user.settings).containsEntry("theme", "dark");
    }

    @Test
    void update_doesNotOverwriteFields_whenDtoFieldIsNull() {
        UUID id   = UUID.randomUUID();
        User user = userWithId(id);
        user.username = "original";
        user.name     = "Original Name";

        UserDto dto = new UserDto();
        dto.id    = id;
        dto.name  = null;  // not updating name
        dto.email = "new@example.com";

        when(userRepo.findByIdOptional(id)).thenReturn(Optional.of(user));
        when(mapper.toDto(user)).thenReturn(new UserDto());

        service.update(dto);

        assertThat(user.username).isEqualTo("original");  // unchanged
        assertThat(user.name).isEqualTo("Original Name"); // unchanged
        assertThat(user.email).isEqualTo("new@example.com");
    }

    @Test
    void update_mergesSettings_whenUserHasExistingSettings() {
        UUID id   = UUID.randomUUID();
        User user = userWithId(id);
        user.settings = new HashMap<>(Map.of("existing", "value"));

        UserDto dto = new UserDto();
        dto.id       = id;
        dto.settings = Map.of("new", "entry");

        when(userRepo.findByIdOptional(id)).thenReturn(Optional.of(user));
        when(mapper.toDto(user)).thenReturn(new UserDto());

        service.update(dto);

        assertThat(user.settings)
                .containsEntry("existing", "value")
                .containsEntry("new", "entry");
    }

    @Test
    void update_publishesUpdateActivity_withBeforeAndAfter() {
        UUID id   = UUID.randomUUID();
        User user = userWithId(id);

        UserDto dto = new UserDto();
        dto.id   = id;
        dto.name = "New Name";

        UserDto before = new UserDto();
        UserDto after  = new UserDto();
        when(userRepo.findByIdOptional(id)).thenReturn(Optional.of(user));
        when(mapper.toDto(user)).thenReturn(before, after);

        service.update(dto);

        verify(activity).publish(eq(ActionType.Update), isNull(), eq(UserDto.class),
                eq(before), eq(after));
    }

    @Test
    void delete_doesNotPublishActivity() {
        UUID id   = UUID.randomUUID();
        User user = userWithId(id);

        when(userRepo.findByIdOptional(id)).thenReturn(Optional.of(user));
        when(mapper.toDto(user)).thenReturn(new UserDto());

        service.delete(id);

        verifyNoInteractions(activity);
    }

    @Test
    void update_throwsNotFound_whenUserMissing() {
        UUID id = UUID.randomUUID();
        UserDto dto = new UserDto();
        dto.id = id;

        when(userRepo.findByIdOptional(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(dto))
                .isInstanceOf(NotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // delete
    // -------------------------------------------------------------------------

    @Test
    void delete_removesUser() {
        UUID id   = UUID.randomUUID();
        User user = userWithId(id);

        when(userRepo.findByIdOptional(id)).thenReturn(Optional.of(user));
        when(mapper.toDto(user)).thenReturn(new UserDto());

        service.delete(id);

        verify(userRepo).delete(user);
    }

    @Test
    void delete_throwsNotFound_whenUserMissing() {
        UUID id = UUID.randomUUID();
        when(userRepo.findByIdOptional(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(id))
                .isInstanceOf(NotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // syncOidcRole
    // -------------------------------------------------------------------------

    private org.eclipse.microprofile.jwt.JsonWebToken jwt(Set<String> groups, String email) {
        var token = mock(org.eclipse.microprofile.jwt.JsonWebToken.class);
        lenient().when(token.getGroups()).thenReturn(groups);
        lenient().when(token.<String>getClaim("email")).thenReturn(email);
        return token;
    }

    @Test
    void syncOidcRole_promotesToAdmin_whenTheAdminGroupIsPresent() {
        UUID id   = UUID.randomUUID();
        User user = userWithId(id);
        user.role = UserRole.User;

        when(userRepo.findById(id)).thenReturn(user);
        when(config.adminGroup()).thenReturn("translatr-admin");
        lenient().when(config.adminEmails()).thenReturn(Set.of());

        service.syncOidcRole(id, jwt(Set.of("translator", "translatr-admin"), "x@example.com"));

        assertThat(user.role).isEqualTo(UserRole.Admin);
    }

    @Test
    void syncOidcRole_promotesToAdmin_whenEmailIsInAdminsList() {
        UUID id   = UUID.randomUUID();
        User user = userWithId(id);
        user.role = UserRole.User;

        when(userRepo.findById(id)).thenReturn(user);
        when(config.adminGroup()).thenReturn("translatr-admin");
        when(config.adminEmails()).thenReturn(Set.of("boss@example.com"));

        service.syncOidcRole(id, jwt(Set.of("translator"), "Boss@Example.com"));

        assertThat(user.role).isEqualTo(UserRole.Admin);
    }

    @Test
    void syncOidcRole_demotesToUser_whenNeitherGroupNorEmailMatches() {
        UUID id   = UUID.randomUUID();
        User user = userWithId(id);
        user.role = UserRole.Admin;

        when(userRepo.findById(id)).thenReturn(user);
        when(config.adminGroup()).thenReturn("translatr-admin");
        when(config.adminEmails()).thenReturn(Set.of("boss@example.com"));

        service.syncOidcRole(id, jwt(Set.of("translator"), "someone@example.com"));

        assertThat(user.role).isEqualTo(UserRole.User);
    }

    @Test
    void syncOidcRole_isSafe_whenGroupsEmptyAndNoEmailClaim() {
        UUID id   = UUID.randomUUID();
        User user = userWithId(id);
        user.role = UserRole.User;

        when(userRepo.findById(id)).thenReturn(user);
        lenient().when(config.adminGroup()).thenReturn("translatr-admin");
        lenient().when(config.adminEmails()).thenReturn(Set.of());

        service.syncOidcRole(id, jwt(Set.of(), null));

        assertThat(user.role).isEqualTo(UserRole.User);
    }

    @Test
    void syncOidcRole_matchesTheKeycloakFullPathForm() {
        UUID id   = UUID.randomUUID();
        User user = userWithId(id);
        user.role = UserRole.User;

        when(userRepo.findById(id)).thenReturn(user);
        when(config.adminGroup()).thenReturn("translatr-admin");
        lenient().when(config.adminEmails()).thenReturn(Set.of());

        service.syncOidcRole(id, jwt(Set.of("/translatr-admin"), null));

        assertThat(user.role).isEqualTo(UserRole.Admin);
    }

    @Test
    void syncOidcRole_leavesRoleUntouched_whenAlreadyCorrect() {
        UUID id   = UUID.randomUUID();
        User user = userWithId(id);
        user.role = UserRole.Admin;

        when(userRepo.findById(id)).thenReturn(user);
        when(config.adminGroup()).thenReturn("translatr-admin");
        lenient().when(config.adminEmails()).thenReturn(Set.of());

        service.syncOidcRole(id, jwt(Set.of("translatr-admin"), null));

        assertThat(user.role).isEqualTo(UserRole.Admin);
    }

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    private static User userWithId(UUID id) {
        User u = new User();
        u.id   = id;
        return u;
    }
}

