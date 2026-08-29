package com.translatr.service;

import com.translatr.criteria.AccessTokenCriteria;
import com.translatr.dto.AccessTokenDto;
import com.translatr.mapper.DtoMapper;
import com.translatr.model.AccessToken;
import com.translatr.model.ActionType;
import com.translatr.model.User;
import com.translatr.repository.AccessTokenRepository;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.ws.rs.NotFoundException;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccessTokenServiceTest {

    @Mock AccessTokenRepository tokenRepo;
    @Mock DtoMapper             mapper;
    @Mock ActivityLogger        activity;

    @InjectMocks AccessTokenService service;

    @SuppressWarnings("unchecked")
    private String runFindAndCaptureQuery(AccessTokenCriteria c, UUID currentUserId) {
        PanacheQuery<AccessToken> query = mock(PanacheQuery.class);
        when(tokenRepo.find(anyString(), any(Object[].class))).thenReturn(query);
        when(query.count()).thenReturn(0L);
        when(query.page(anyInt(), anyInt())).thenReturn(query);
        when(query.list()).thenReturn(List.of());

        service.find(c, currentUserId);

        ArgumentCaptor<String> ql = ArgumentCaptor.forClass(String.class);
        verify(tokenRepo).find(ql.capture(), any(Object[].class));
        return ql.getValue();
    }

    @Test
    void find_scopesToCurrentUser_andTranslatesSearchAndOrder() {
        AccessTokenCriteria c = new AccessTokenCriteria();
        c.search = "ci";
        c.order  = "whenUpdated desc";
        c.limit  = 20;

        String ql = runFindAndCaptureQuery(c, UUID.randomUUID());
        assertThat(ql).startsWith("user.id = ?1");
        assertThat(ql).contains("lower(name) LIKE ");
        assertThat(ql).endsWith("ORDER BY whenUpdated DESC");
    }

    @Test
    void get_throwsNotFound_whenTokenMissing() {
        when(tokenRepo.findByIdOptional(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(42L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void create_generatesKeyAndPersists() {
        User owner = new User();
        owner.id   = UUID.randomUUID();

        AccessTokenDto dto = new AccessTokenDto();
        dto.name  = "my-token";
        dto.scope = "read";

        when(mapper.toDto(any(AccessToken.class))).thenAnswer(inv -> {
            AccessToken t = inv.getArgument(0);
            AccessTokenDto result = new AccessTokenDto();
            result.name  = t.name;
            result.scope = t.scope;
            result.key   = t.key;
            return result;
        });

        AccessTokenDto result = service.create(dto, owner);

        verify(tokenRepo).persist(any(AccessToken.class));
        assertThat(result.name).isEqualTo("my-token");
        assertThat(result.scope).isEqualTo("read");
        assertThat(result.key).isNotBlank().doesNotContain("-"); // UUID stripped of dashes
    }

    @Test
    void create_publishesCreateActivity_withNoProject() {
        User owner = new User();
        owner.id = UUID.randomUUID();

        AccessTokenDto dto = new AccessTokenDto();
        dto.name = "my-token";

        AccessTokenDto after = new AccessTokenDto();
        when(mapper.toDto(any(AccessToken.class))).thenReturn(after);

        service.create(dto, owner);

        verify(activity).publish(eq(ActionType.Create), isNull(), eq(AccessTokenDto.class),
                isNull(), eq(after));
    }

    @Test
    void update_publishesUpdateActivity_withBeforeAndAfter() {
        AccessToken token = new AccessToken();
        token.id = 1L;

        AccessTokenDto dto = new AccessTokenDto();
        dto.id   = 1L;
        dto.name = "updated-name";

        AccessTokenDto before = new AccessTokenDto();
        AccessTokenDto after  = new AccessTokenDto();
        when(tokenRepo.findByIdOptional(1L)).thenReturn(Optional.of(token));
        when(mapper.toDto(token)).thenReturn(before, after);

        service.update(dto);

        verify(activity).publish(eq(ActionType.Update), isNull(), eq(AccessTokenDto.class),
                eq(before), eq(after));
    }

    @Test
    void delete_doesNotPublishActivity() {
        AccessToken token = new AccessToken();
        token.id = 5L;

        when(tokenRepo.findByIdOptional(5L)).thenReturn(Optional.of(token));
        when(mapper.toDto(token)).thenReturn(new AccessTokenDto());

        service.delete(5L);

        verifyNoInteractions(activity);
    }

    @Test
    void update_appliesNameAndScope() {
        AccessToken token = new AccessToken();
        token.id    = 1L;
        token.name  = "old";
        token.scope = "read";

        AccessTokenDto dto = new AccessTokenDto();
        dto.id    = 1L;
        dto.name  = "updated-name";
        dto.scope = "read,write";

        when(tokenRepo.findByIdOptional(1L)).thenReturn(Optional.of(token));
        when(mapper.toDto(token)).thenReturn(dto);

        service.update(dto);

        assertThat(token.name).isEqualTo("updated-name");
        assertThat(token.scope).isEqualTo("read,write");
    }

    @Test
    void update_doesNotOverwriteFields_whenDtoFieldIsNull() {
        AccessToken token = new AccessToken();
        token.id    = 2L;
        token.name  = "original";
        token.scope = "read";

        AccessTokenDto dto = new AccessTokenDto();
        dto.id   = 2L;
        dto.name = null; // not updating

        when(tokenRepo.findByIdOptional(2L)).thenReturn(Optional.of(token));
        when(mapper.toDto(token)).thenReturn(new AccessTokenDto());

        service.update(dto);

        assertThat(token.name).isEqualTo("original");
        assertThat(token.scope).isEqualTo("read");
    }

    @Test
    void update_throwsNotFound_whenTokenMissing() {
        AccessTokenDto dto = new AccessTokenDto();
        dto.id = 99L;

        when(tokenRepo.findByIdOptional(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(dto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_removesToken() {
        AccessToken token = new AccessToken();
        token.id = 5L;

        when(tokenRepo.findByIdOptional(5L)).thenReturn(Optional.of(token));
        when(mapper.toDto(token)).thenReturn(new AccessTokenDto());

        service.delete(5L);

        verify(tokenRepo).delete(token);
    }

    @Test
    void findUserByKey_returnsUser_whenTokenExists() {
        User user = new User();
        user.id   = UUID.randomUUID();

        AccessToken token = new AccessToken();
        token.user = user;

        when(tokenRepo.findByKey("secret-key")).thenReturn(Optional.of(token));

        Optional<User> result = service.findUserByKey("secret-key");

        assertThat(result).isPresent().contains(user);
    }

    @Test
    void findUserByKey_returnsEmpty_whenTokenNotFound() {
        when(tokenRepo.findByKey("unknown")).thenReturn(Optional.empty());

        Optional<User> result = service.findUserByKey("unknown");

        assertThat(result).isEmpty();
    }
}



