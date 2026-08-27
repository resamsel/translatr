package com.translatr.service;

import com.translatr.dto.KeyDto;
import com.translatr.mapper.DtoMapper;
import com.translatr.model.ActionType;
import com.translatr.model.Key;
import com.translatr.model.Project;
import com.translatr.repository.KeyRepository;
import com.translatr.repository.ProjectRepository;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KeyServiceTest {

    @Mock KeyRepository         keyRepo;
    @Mock ProjectRepository     projectRepo;
    @Mock DtoMapper             mapper;
    @Mock ActivityLogger        activity;

    @InjectMocks KeyService service;

    @Test
    void get_throwsNotFound_whenKeyMissing() {
        UUID id = UUID.randomUUID();
        when(keyRepo.findByIdOptional(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(id))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void create_persistsKey() {
        UUID projectId = UUID.randomUUID();
        Project project = new Project("proj");
        project.id = projectId;

        KeyDto dto = new KeyDto();
        dto.projectId = projectId;
        dto.name      = "greeting";

        when(projectRepo.findByIdOptional(projectId)).thenReturn(Optional.of(project));
        when(mapper.toDto(any(Key.class))).thenAnswer(inv -> {
            Key k = inv.getArgument(0);
            KeyDto result = new KeyDto();
            result.name = k.name;
            return result;
        });

        KeyDto result = service.create(dto);

        verify(keyRepo).persist(any(Key.class));
        assertThat(result.name).isEqualTo("greeting");
    }

    @Test
    void create_publishesCreateActivity() {
        UUID projectId = UUID.randomUUID();
        Project project = new Project("proj");
        project.id = projectId;

        KeyDto dto = new KeyDto();
        dto.projectId = projectId;
        dto.name      = "greeting";

        KeyDto after = new KeyDto();
        when(projectRepo.findByIdOptional(projectId)).thenReturn(Optional.of(project));
        when(mapper.toDto(any(Key.class))).thenReturn(after);

        service.create(dto);

        verify(activity).publish(eq(ActionType.Create), eq(project), eq(KeyDto.class), isNull(), eq(after));
    }

    @Test
    void create_throwsNotFound_whenProjectMissing() {
        UUID projectId = UUID.randomUUID();
        KeyDto dto = new KeyDto();
        dto.projectId = projectId;
        dto.name      = "greeting";

        when(projectRepo.findByIdOptional(projectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(NotFoundException.class);
        verifyNoInteractions(activity);
    }

    @Test
    void update_appliesName() {
        UUID id = UUID.randomUUID();
        Key key = new Key();
        key.id  = id;

        KeyDto dto = new KeyDto();
        dto.id   = id;
        dto.name = "updated-key";

        when(keyRepo.findByIdOptional(id)).thenReturn(Optional.of(key));
        when(mapper.toDto(key)).thenReturn(dto);

        service.update(dto);

        assertThat(key.name).isEqualTo("updated-key");
    }

    @Test
    void update_publishesUpdateActivity_withBeforeAndAfter() {
        UUID id = UUID.randomUUID();
        Project project = new Project("proj");
        Key key = new Key();
        key.id      = id;
        key.project = project;

        KeyDto dto = new KeyDto();
        dto.id   = id;
        dto.name = "updated-key";

        KeyDto before = new KeyDto();
        KeyDto after  = new KeyDto();
        when(keyRepo.findByIdOptional(id)).thenReturn(Optional.of(key));
        when(mapper.toDto(key)).thenReturn(before, after);

        service.update(dto);

        verify(activity).publish(eq(ActionType.Update), eq(project), eq(KeyDto.class), eq(before), eq(after));
    }

    @Test
    void update_throwsNotFound_whenKeyMissing() {
        UUID id = UUID.randomUUID();
        KeyDto dto = new KeyDto();
        dto.id = id;

        when(keyRepo.findByIdOptional(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(dto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_removesKey() {
        UUID id = UUID.randomUUID();
        Key key = new Key();
        key.id  = id;

        when(keyRepo.findByIdOptional(id)).thenReturn(Optional.of(key));
        when(mapper.toDto(key)).thenReturn(new KeyDto());

        service.delete(id);

        verify(keyRepo).delete(key);
    }

    @Test
    void delete_publishesDeleteActivity_withBeforeSnapshot() {
        UUID id = UUID.randomUUID();
        Project project = new Project("proj");
        Key key = new Key();
        key.id      = id;
        key.project = project;

        KeyDto before = new KeyDto();
        when(keyRepo.findByIdOptional(id)).thenReturn(Optional.of(key));
        when(mapper.toDto(key)).thenReturn(before);

        service.delete(id);

        verify(activity).publish(eq(ActionType.Delete), eq(project), eq(KeyDto.class), eq(before), isNull());
    }
}
