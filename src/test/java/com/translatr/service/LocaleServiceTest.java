package com.translatr.service;

import com.translatr.dto.LocaleDto;
import com.translatr.mapper.DtoMapper;
import com.translatr.model.ActionType;
import com.translatr.model.Locale;
import com.translatr.model.Project;
import com.translatr.repository.LocaleRepository;
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
class LocaleServiceTest {

    @Mock LocaleRepository  localeRepo;
    @Mock ProjectRepository projectRepo;
    @Mock DtoMapper         mapper;
    @Mock ActivityLogger    activity;

    @InjectMocks LocaleService service;

    @Test
    void get_throwsNotFound_whenLocaleMissing() {
        UUID id = UUID.randomUUID();
        when(localeRepo.findByIdOptional(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(id))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void create_persistsLocale() {
        UUID projectId = UUID.randomUUID();
        Project project = new Project("proj");
        project.id = projectId;

        LocaleDto dto = new LocaleDto();
        dto.projectId = projectId;
        dto.name      = "de";

        when(projectRepo.findByIdOptional(projectId)).thenReturn(Optional.of(project));
        when(mapper.toDto(any(Locale.class))).thenAnswer(inv -> {
            Locale l = inv.getArgument(0);
            LocaleDto result = new LocaleDto();
            result.name = l.name;
            return result;
        });

        LocaleDto result = service.create(dto);

        verify(localeRepo).persist(any(Locale.class));
        assertThat(result.name).isEqualTo("de");
    }

    @Test
    void create_publishesCreateActivity_forTheProject() {
        UUID projectId = UUID.randomUUID();
        Project project = new Project("proj");
        project.id = projectId;

        LocaleDto dto = new LocaleDto();
        dto.projectId = projectId;
        dto.name      = "de";

        LocaleDto after = new LocaleDto();
        when(projectRepo.findByIdOptional(projectId)).thenReturn(Optional.of(project));
        when(mapper.toDto(any(Locale.class))).thenReturn(after);

        service.create(dto);

        verify(activity).publish(eq(ActionType.Create), eq(project), eq(LocaleDto.class),
                isNull(), eq(after));
    }

    @Test
    void update_publishesUpdateActivity_withBeforeAndAfter() {
        UUID id = UUID.randomUUID();
        Project project = new Project("proj");
        Locale locale = new Locale();
        locale.id      = id;
        locale.project = project;

        LocaleDto dto = new LocaleDto();
        dto.id   = id;
        dto.name = "fr";

        LocaleDto before = new LocaleDto();
        LocaleDto after  = new LocaleDto();
        when(localeRepo.findByIdOptional(id)).thenReturn(Optional.of(locale));
        when(mapper.toDto(locale)).thenReturn(before, after);

        service.update(dto);

        verify(activity).publish(eq(ActionType.Update), eq(project), eq(LocaleDto.class),
                eq(before), eq(after));
    }

    @Test
    void delete_publishesDeleteActivity_withBeforeSnapshot() {
        UUID id = UUID.randomUUID();
        Project project = new Project("proj");
        Locale locale = new Locale();
        locale.id      = id;
        locale.project = project;

        LocaleDto before = new LocaleDto();
        when(localeRepo.findByIdOptional(id)).thenReturn(Optional.of(locale));
        when(mapper.toDto(locale)).thenReturn(before);

        service.delete(id);

        verify(activity).publish(eq(ActionType.Delete), eq(project), eq(LocaleDto.class),
                eq(before), isNull());
    }

    @Test
    void create_throwsNotFound_whenProjectMissing() {
        UUID projectId = UUID.randomUUID();
        LocaleDto dto = new LocaleDto();
        dto.projectId = projectId;
        dto.name      = "de";

        when(projectRepo.findByIdOptional(projectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void update_appliesName() {
        UUID id     = UUID.randomUUID();
        Locale locale = new Locale();
        locale.id   = id;

        LocaleDto dto = new LocaleDto();
        dto.id   = id;
        dto.name = "fr";

        when(localeRepo.findByIdOptional(id)).thenReturn(Optional.of(locale));
        when(mapper.toDto(locale)).thenReturn(dto);

        service.update(dto);

        assertThat(locale.name).isEqualTo("fr");
    }

    @Test
    void update_throwsNotFound_whenLocaleMissing() {
        UUID id = UUID.randomUUID();
        LocaleDto dto = new LocaleDto();
        dto.id = id;

        when(localeRepo.findByIdOptional(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(dto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_removesLocale() {
        UUID id     = UUID.randomUUID();
        Locale locale = new Locale();
        locale.id   = id;

        when(localeRepo.findByIdOptional(id)).thenReturn(Optional.of(locale));
        when(mapper.toDto(locale)).thenReturn(new LocaleDto());

        service.delete(id);

        verify(localeRepo).delete(locale);
    }
}

