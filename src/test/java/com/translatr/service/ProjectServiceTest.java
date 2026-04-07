package com.translatr.service;

import com.translatr.dto.ProjectDto;
import com.translatr.mapper.DtoMapper;
import com.translatr.model.Project;
import com.translatr.model.User;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock ProjectRepository projectRepo;
    @Mock DtoMapper         mapper;

    @InjectMocks ProjectService service;

    // -------------------------------------------------------------------------
    // get
    // -------------------------------------------------------------------------

    @Test
    void get_throwsNotFound_whenProjectMissing() {
        UUID id = UUID.randomUUID();
        when(projectRepo.findByIdOptional(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(id))
                .isInstanceOf(NotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // create
    // -------------------------------------------------------------------------

    @Test
    void create_setsOwnerAndPersists() {
        User owner = new User();
        owner.id   = UUID.randomUUID();
        owner.name = "John";

        ProjectDto dto = new ProjectDto();
        dto.name        = "my-project";
        dto.description = "A test project";

        when(mapper.toDto(any(Project.class))).thenAnswer(inv -> {
            Project p = inv.getArgument(0);
            ProjectDto result = new ProjectDto();
            result.name        = p.name;
            result.description = p.description;
            return result;
        });

        ProjectDto result = service.create(dto, owner);

        verify(projectRepo).persist(any(Project.class));
        assertThat(result.name).isEqualTo("my-project");
        assertThat(result.description).isEqualTo("A test project");
    }

    // -------------------------------------------------------------------------
    // update
    // -------------------------------------------------------------------------

    @Test
    void update_appliesNameAndDescription() {
        UUID id      = UUID.randomUUID();
        Project proj = projectWithId(id);

        ProjectDto dto   = new ProjectDto();
        dto.id           = id;
        dto.name         = "updated-name";
        dto.description  = "updated-desc";

        when(projectRepo.findByIdOptional(id)).thenReturn(Optional.of(proj));
        when(mapper.toDto(proj)).thenReturn(dto);

        service.update(dto);

        assertThat(proj.name).isEqualTo("updated-name");
        assertThat(proj.description).isEqualTo("updated-desc");
    }

    @Test
    void update_doesNotOverwriteFields_whenDtoFieldIsNull() {
        UUID id      = UUID.randomUUID();
        Project proj = projectWithId(id);
        proj.name        = "original";
        proj.description = "original-desc";

        ProjectDto dto = new ProjectDto();
        dto.id          = id;
        dto.name        = null;

        when(projectRepo.findByIdOptional(id)).thenReturn(Optional.of(proj));
        when(mapper.toDto(proj)).thenReturn(new ProjectDto());

        service.update(dto);

        assertThat(proj.name).isEqualTo("original");
        assertThat(proj.description).isEqualTo("original-desc");
    }

    @Test
    void update_throwsNotFound_whenProjectMissing() {
        UUID id = UUID.randomUUID();
        ProjectDto dto = new ProjectDto();
        dto.id = id;

        when(projectRepo.findByIdOptional(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(dto))
                .isInstanceOf(NotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // delete (soft-delete)
    // -------------------------------------------------------------------------

    @Test
    void delete_setsFlagAndDoesNotRemoveEntity() {
        UUID id      = UUID.randomUUID();
        Project proj = projectWithId(id);
        proj.deleted = false;

        when(projectRepo.findByIdOptional(id)).thenReturn(Optional.of(proj));
        when(mapper.toDto(proj)).thenReturn(new ProjectDto());

        service.delete(id);

        assertThat(proj.deleted).isTrue();
        verify(projectRepo, never()).delete(proj);
    }

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    private static Project projectWithId(UUID id) {
        Project p = new Project();
        p.id = id;
        return p;
    }
}



