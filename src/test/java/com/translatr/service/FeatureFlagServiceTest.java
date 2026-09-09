package com.translatr.service;

import com.translatr.criteria.FeatureFlagCriteria;
import com.translatr.dto.FeatureFlagDto;
import com.translatr.mapper.DtoMapper;
import com.translatr.model.User;
import com.translatr.model.UserFeatureFlag;
import com.translatr.model.UserRole;
import com.translatr.repository.UserFeatureFlagRepository;
import com.translatr.repository.UserRepository;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.ws.rs.ForbiddenException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeatureFlagServiceTest {

    @Mock UserFeatureFlagRepository featureFlagRepo;
    @Mock UserRepository            userRepo;
    @Mock DtoMapper                 mapper;

    @InjectMocks FeatureFlagService service;

    @SuppressWarnings("unchecked")
    private String runFindAndCaptureQuery(FeatureFlagCriteria c, UUID currentUserId) {
        PanacheQuery<UserFeatureFlag> query = mock(PanacheQuery.class);
        when(featureFlagRepo.find(anyString(), any(Object[].class))).thenReturn(query);
        when(query.count()).thenReturn(0L);
        when(query.page(anyInt(), anyInt())).thenReturn(query);
        when(query.list()).thenReturn(List.of());

        service.find(c, currentUserId);

        ArgumentCaptor<String> ql = ArgumentCaptor.forClass(String.class);
        verify(featureFlagRepo).find(ql.capture(), any(Object[].class));
        return ql.getValue();
    }

    @Test
    void find_scopesToCurrentUser_andTranslatesFeatureFilter() {
        FeatureFlagCriteria c = new FeatureFlagCriteria();
        c.feature = "beta-editor";
        c.limit   = 20;

        String ql = runFindAndCaptureQuery(c, UUID.randomUUID());
        assertThat(ql).startsWith("user.id = ?1");
        assertThat(ql).contains("feature = ");
    }

    private static User userWithId(UUID id, UserRole role) {
        User u = new User();
        u.id   = id;
        u.role = role;
        return u;
    }

    @Test
    void create_allowsCallerToCreateTheirOwnOverride() {
        UUID callerId = UUID.randomUUID();
        User caller   = userWithId(callerId, UserRole.User);
        FeatureFlagDto dto = new FeatureFlagDto().userId(callerId).feature("beta-editor").enabled(true);

        when(userRepo.findByIdOptional(callerId)).thenReturn(Optional.of(userWithId(callerId, UserRole.User)));
        when(mapper.toDto(any(UserFeatureFlag.class))).thenReturn(dto);

        service.create(dto, caller);

        verify(featureFlagRepo).persist(any(UserFeatureFlag.class));
    }

    @Test
    void create_deniedForAnotherUsersOverride_whenCallerIsNotAdmin() {
        User caller = userWithId(UUID.randomUUID(), UserRole.User);
        FeatureFlagDto dto = new FeatureFlagDto().userId(UUID.randomUUID()).feature("beta-editor").enabled(true);

        assertThatThrownBy(() -> service.create(dto, caller)).isInstanceOf(ForbiddenException.class);
        verifyNoInteractions(featureFlagRepo);
    }

    @Test
    void create_allowedForAnotherUsersOverride_whenCallerIsAdmin() {
        User caller = userWithId(UUID.randomUUID(), UserRole.Admin);
        UUID targetId = UUID.randomUUID();
        FeatureFlagDto dto = new FeatureFlagDto().userId(targetId).feature("beta-editor").enabled(true);

        when(userRepo.findByIdOptional(targetId)).thenReturn(Optional.of(userWithId(targetId, UserRole.User)));
        when(mapper.toDto(any(UserFeatureFlag.class))).thenReturn(dto);

        service.create(dto, caller);

        verify(featureFlagRepo).persist(any(UserFeatureFlag.class));
    }

    @Test
    void update_deniedForAnotherUsersOverride_whenCallerIsNotAdmin() {
        UUID flagId  = UUID.randomUUID();
        User caller  = userWithId(UUID.randomUUID(), UserRole.User);
        UserFeatureFlag flag = UserFeatureFlag.of(flagId, userWithId(UUID.randomUUID(), UserRole.User), "beta-editor", false);

        when(featureFlagRepo.findByIdOptional(flagId)).thenReturn(Optional.of(flag));

        assertThatThrownBy(() -> service.update(new FeatureFlagDto().id(flagId).enabled(true), caller))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void update_allowedForAnotherUsersOverride_whenCallerIsAdmin() {
        UUID flagId  = UUID.randomUUID();
        User caller  = userWithId(UUID.randomUUID(), UserRole.Admin);
        FeatureFlagDto dto = new FeatureFlagDto().id(flagId).enabled(true);
        UserFeatureFlag flag = UserFeatureFlag.of(flagId, userWithId(UUID.randomUUID(), UserRole.User), "beta-editor", false);

        when(featureFlagRepo.findByIdOptional(flagId)).thenReturn(Optional.of(flag));
        when(mapper.toDto(flag)).thenReturn(dto);

        service.update(dto, caller);

        assertThat(flag.enabled).isTrue();
    }

    @Test
    void delete_deniedForAnotherUsersOverride_whenCallerIsNotAdmin() {
        UUID flagId  = UUID.randomUUID();
        User caller  = userWithId(UUID.randomUUID(), UserRole.User);
        UserFeatureFlag flag = UserFeatureFlag.of(flagId, userWithId(UUID.randomUUID(), UserRole.User), "beta-editor", false);

        when(featureFlagRepo.findByIdOptional(flagId)).thenReturn(Optional.of(flag));

        assertThatThrownBy(() -> service.delete(flagId, caller)).isInstanceOf(ForbiddenException.class);
        verify(featureFlagRepo, never()).delete(any(UserFeatureFlag.class));
    }

    @Test
    void delete_allowedForAnotherUsersOverride_whenCallerIsAdmin() {
        UUID flagId  = UUID.randomUUID();
        User caller  = userWithId(UUID.randomUUID(), UserRole.Admin);
        UserFeatureFlag flag = UserFeatureFlag.of(flagId, userWithId(UUID.randomUUID(), UserRole.User), "beta-editor", false);

        when(featureFlagRepo.findByIdOptional(flagId)).thenReturn(Optional.of(flag));
        when(mapper.toDto(flag)).thenReturn(new FeatureFlagDto().id(flagId));

        service.delete(flagId, caller);

        verify(featureFlagRepo).delete(flag);
    }
}
