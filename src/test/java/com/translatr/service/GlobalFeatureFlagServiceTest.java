package com.translatr.service;

import com.translatr.dto.GlobalFeatureFlagDto;
import com.translatr.mapper.DtoMapper;
import com.translatr.model.FeatureFlag;
import com.translatr.repository.FeatureFlagRepository;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalFeatureFlagServiceTest {

    @Mock FeatureFlagRepository repo;
    @Mock DtoMapper             mapper;

    @InjectMocks GlobalFeatureFlagService service;

    @Test
    void set_rejectsUnknownFeature() {
        assertThatThrownBy(() -> service.set("no-such-feature", true))
            .isInstanceOf(BadRequestException.class);
        verify(repo, never()).persist(any(FeatureFlag.class));
    }

    @Test
    void set_insertsWhenNoRowExists() {
        when(repo.findByFeature("header-graphic")).thenReturn(Optional.empty());
        when(mapper.toDto(any(FeatureFlag.class))).thenReturn(new GlobalFeatureFlagDto());

        service.set("header-graphic", true);

        verify(repo).persist(argThat((FeatureFlag f) ->
            f.feature.equals("header-graphic") && f.enabled));
    }

    @Test
    void set_updatesExistingRowInPlace() {
        FeatureFlag existing = FeatureFlag.of("header-graphic", false);
        when(repo.findByFeature("header-graphic")).thenReturn(Optional.of(existing));
        when(mapper.toDto(existing)).thenReturn(new GlobalFeatureFlagDto());

        service.set("header-graphic", true);

        assertThat(existing.enabled).isTrue();
        verify(repo, never()).persist(any(FeatureFlag.class));
    }

    @Test
    void delete_throwsNotFound_whenAbsent() {
        UUID id = UUID.randomUUID();
        when(repo.findByIdOptional(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(id)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_removesTheRow() {
        UUID id = UUID.randomUUID();
        FeatureFlag flag = FeatureFlag.of("header-graphic", true);
        when(repo.findByIdOptional(id)).thenReturn(Optional.of(flag));

        service.delete(id);

        verify(repo).delete(flag);
    }
}
