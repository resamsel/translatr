package com.translatr.service;

import com.translatr.criteria.FeatureFlagCriteria;
import com.translatr.mapper.DtoMapper;
import com.translatr.model.UserFeatureFlag;
import com.translatr.repository.UserFeatureFlagRepository;
import com.translatr.repository.UserRepository;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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
}
