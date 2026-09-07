package com.translatr.service;

import com.translatr.model.Feature;
import com.translatr.model.FeatureFlag;
import com.translatr.model.UserFeatureFlag;
import com.translatr.repository.FeatureFlagRepository;
import com.translatr.repository.UserFeatureFlagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeatureResolverTest {

    @Mock UserFeatureFlagRepository userFlagRepo;
    @Mock FeatureFlagRepository     globalFlagRepo;

    @InjectMocks FeatureResolver resolver;

    private final UUID userId = UUID.randomUUID();

    private UserFeatureFlag userFlag(String feature, boolean enabled) {
        return UserFeatureFlag.of(null, null, feature, enabled);
    }

    @Test
    void resolveAll_returnsOneEntryPerFeature_defaultingToHardcodedFalse() {
        when(userFlagRepo.listByUser(userId)).thenReturn(List.of());
        when(globalFlagRepo.listAll()).thenReturn(List.of());

        Map<String, Boolean> result = resolver.resolveAll(userId);

        assertThat(result).hasSize(Feature.values().length);
        assertThat(result).containsEntry("language-switcher", false);
        assertThat(result).containsEntry("project-cli-card", false);
    }

    @Test
    void resolveAll_globalRowWinsOverDefault() {
        when(userFlagRepo.listByUser(userId)).thenReturn(List.of());
        when(globalFlagRepo.listAll()).thenReturn(List.of(FeatureFlag.of("header-graphic", true)));

        assertThat(resolver.resolveAll(userId)).containsEntry("header-graphic", true);
    }

    @Test
    void resolveAll_userOverrideWinsOverGlobalAndDefault() {
        when(userFlagRepo.listByUser(userId))
            .thenReturn(List.of(userFlag("header-graphic", false)));
        when(globalFlagRepo.listAll()).thenReturn(List.of(FeatureFlag.of("header-graphic", true)));

        assertThat(resolver.resolveAll(userId)).containsEntry("header-graphic", false);
    }

    @Test
    void resolveAll_ignoresStaleFeatureStringsNotInEnum() {
        when(userFlagRepo.listByUser(userId)).thenReturn(List.of(userFlag("legacy-flag", true)));
        when(globalFlagRepo.listAll()).thenReturn(List.of());

        Map<String, Boolean> result = resolver.resolveAll(userId);

        assertThat(result).doesNotContainKey("legacy-flag");
        assertThat(result).hasSize(Feature.values().length);
    }

    @Test
    void resolveDetail_reportsDefaultGlobalOverrideAndEffective() {
        UUID overrideId = UUID.randomUUID();
        UserFeatureFlag override = UserFeatureFlag.of(overrideId, null, "language-switcher", true);
        when(userFlagRepo.listByUser(userId)).thenReturn(List.of(override));
        when(globalFlagRepo.listAll())
            .thenReturn(List.of(com.translatr.model.FeatureFlag.of("header-graphic", true)));

        var detail = resolver.resolveDetail(userId);

        assertThat(detail).hasSize(Feature.values().length);

        var ls = detail.stream().filter(d -> d.getFeature().equals("language-switcher")).findFirst().orElseThrow();
        assertThat(ls.getDefaultEnabled()).isFalse();
        assertThat(ls.getGlobal()).isNull();
        assertThat(ls.getUserOverride()).isTrue();
        assertThat(ls.getUserOverrideId()).isEqualTo(overrideId);
        assertThat(ls.getEffective()).isTrue();

        var hg = detail.stream().filter(d -> d.getFeature().equals("header-graphic")).findFirst().orElseThrow();
        assertThat(hg.getGlobal()).isTrue();
        assertThat(hg.getUserOverride()).isNull();
        assertThat(hg.getUserOverrideId()).isNull();
        assertThat(hg.getEffective()).isTrue();

        var cli = detail.stream().filter(d -> d.getFeature().equals("project-cli-card")).findFirst().orElseThrow();
        assertThat(cli.getGlobal()).isNull();
        assertThat(cli.getUserOverride()).isNull();
        assertThat(cli.getEffective()).isFalse();
    }
}
