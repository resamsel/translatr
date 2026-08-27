package com.translatr.service;

import com.translatr.dto.UserDto;
import com.translatr.mapper.DtoMapper;
import com.translatr.model.User;
import com.translatr.repository.UserFeatureFlagRepository;
import com.translatr.repository.UserRepository;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository            userRepo;
    @Mock UserFeatureFlagRepository featureFlagRepo;
    @Mock DtoMapper                 mapper;

    @InjectMocks UserService service;

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
        when(featureFlagRepo.listByUser(id)).thenReturn(Collections.emptyList());

        UserDto result = service.get(id);

        assertThat(result.id).isEqualTo(id);
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
    // helpers
    // -------------------------------------------------------------------------

    private static User userWithId(UUID id) {
        User u = new User();
        u.id   = id;
        return u;
    }
}

