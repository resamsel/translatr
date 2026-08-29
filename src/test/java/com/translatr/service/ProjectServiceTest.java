package com.translatr.service;

import com.translatr.criteria.ProjectCriteria;
import com.translatr.dto.MemberDto;
import com.translatr.dto.ProjectDto;
import com.translatr.mapper.DtoMapper;
import com.translatr.model.ActionType;
import com.translatr.model.Project;
import com.translatr.model.ProjectRole;
import com.translatr.model.ProjectUser;
import com.translatr.model.User;
import com.translatr.repository.ProjectRepository;
import com.translatr.repository.ProjectUserRepository;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
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
class ProjectServiceTest {

    @Mock ProjectRepository     projectRepo;
    @Mock ProjectUserRepository memberRepo;
    @Mock DtoMapper             mapper;
    @Mock ActivityLogger        activity;
    @Mock ProgressService       progress;

    @InjectMocks ProjectService service;

    @SuppressWarnings("unchecked")
    private String runFindAndCaptureQuery(ProjectCriteria c) {
        PanacheQuery<Project> query = mock(PanacheQuery.class);
        when(projectRepo.find(anyString(), any(Object[].class))).thenReturn(query);
        when(query.count()).thenReturn(0L);
        when(query.page(anyInt(), anyInt())).thenReturn(query);
        when(query.list()).thenReturn(List.of());

        service.find(c);

        ArgumentCaptor<String> ql = ArgumentCaptor.forClass(String.class);
        verify(projectRepo).find(ql.capture(), any(Object[].class));
        return ql.getValue();
    }

    @Test
    void find_translatesOwnerIdMemberIdNameAndSearch() {
        ProjectCriteria c = new ProjectCriteria();
        c.ownerId  = UUID.randomUUID();
        c.memberId = UUID.randomUUID();
        c.name     = "acme";
        c.search   = "acm";
        c.limit    = 20;

        String ql = runFindAndCaptureQuery(c);
        assertThat(ql).contains("p.owner.id = ");
        assertThat(ql).contains("EXISTS (SELECT 1 FROM ProjectUser pu WHERE pu.project = p AND pu.user.id = ");
        assertThat(ql).contains("p.name = ");
        assertThat(ql).contains("lower(p.description) LIKE ");
    }

    @Test
    @SuppressWarnings("unchecked")
    void find_withFetchProgress_populatesProjectProgressFromProgressService() {
        UUID id = UUID.randomUUID();
        Project p = projectWithId(id);
        ProjectDto dto = new ProjectDto();
        dto.id = id;

        PanacheQuery<Project> query = mock(PanacheQuery.class);
        when(projectRepo.find(anyString(), any(Object[].class))).thenReturn(query);
        when(query.count()).thenReturn(1L);
        when(query.page(anyInt(), anyInt())).thenReturn(query);
        when(query.list()).thenReturn(List.of(p));
        when(mapper.toDto(p)).thenReturn(dto);
        when(progress.projectProgress(List.of(id))).thenReturn(Map.of(id, 0.4));

        ProjectCriteria c = new ProjectCriteria();
        c.fetch = "progress";
        c.limit = 20;

        var result = service.find(c);

        assertThat(result.list.get(0).progress).isEqualTo(0.4);
    }

    @Test
    @SuppressWarnings("unchecked")
    void find_withoutFetchProgress_leavesProgressNullAndNeverQueriesProgress() {
        UUID id = UUID.randomUUID();
        Project p = projectWithId(id);
        ProjectDto dto = new ProjectDto();
        dto.id = id;

        PanacheQuery<Project> query = mock(PanacheQuery.class);
        when(projectRepo.find(anyString(), any(Object[].class))).thenReturn(query);
        when(query.count()).thenReturn(1L);
        when(query.page(anyInt(), anyInt())).thenReturn(query);
        when(query.list()).thenReturn(List.of(p));
        when(mapper.toDto(p)).thenReturn(dto);

        ProjectCriteria c = new ProjectCriteria();
        c.limit = 20;

        var result = service.find(c);

        assertThat(result.list.get(0).progress).isNull();
        verify(progress, never()).projectProgress(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void find_withFetchMembers_populatesMembers() {
        UUID id = UUID.randomUUID();
        Project p = projectWithId(id);
        ProjectDto dto = new ProjectDto();
        dto.id = id;

        ProjectUser pu = new ProjectUser(ProjectRole.Manager);
        MemberDto memberDto = new MemberDto();

        PanacheQuery<Project> query = mock(PanacheQuery.class);
        when(projectRepo.find(anyString(), any(Object[].class))).thenReturn(query);
        when(query.count()).thenReturn(1L);
        when(query.page(anyInt(), anyInt())).thenReturn(query);
        when(query.list()).thenReturn(List.of(p));
        when(mapper.toDto(p)).thenReturn(dto);
        when(memberRepo.list("project.id", id)).thenReturn(List.of(pu));
        when(mapper.toDto(pu)).thenReturn(memberDto);

        ProjectCriteria c = new ProjectCriteria();
        c.fetch = "members";
        c.limit = 20;

        var result = service.find(c);

        assertThat(result.list.get(0).members).containsExactly(memberDto);
    }

    // -------------------------------------------------------------------------
    // getByOwnerAndName
    // -------------------------------------------------------------------------

    @Test
    void getByOwnerAndName_withFetchMyrole_setsMyRoleFromMembership() {
        UUID projectId = UUID.randomUUID();
        UUID userId    = UUID.randomUUID();
        Project p = projectWithId(projectId);
        ProjectDto dto = new ProjectDto();
        dto.id = projectId;

        ProjectUser membership = new ProjectUser(ProjectRole.Manager);
        when(projectRepo.findByOwnerUsernameAndName("alice", "acme")).thenReturn(Optional.of(p));
        when(mapper.toDto(p)).thenReturn(dto);
        when(memberRepo.findByProjectAndUser(projectId, userId)).thenReturn(Optional.of(membership));

        ProjectDto result = service.getByOwnerAndName("alice", "acme", "myrole", userId);

        assertThat(result.myRole).isEqualTo("Manager");
    }

    @Test
    void getByOwnerAndName_withoutFetch_leavesMyRoleNullAndDoesNotQueryMembership() {
        UUID projectId = UUID.randomUUID();
        Project p = projectWithId(projectId);
        ProjectDto dto = new ProjectDto();
        dto.id = projectId;

        when(projectRepo.findByOwnerUsernameAndName("alice", "acme")).thenReturn(Optional.of(p));
        when(mapper.toDto(p)).thenReturn(dto);

        ProjectDto result = service.getByOwnerAndName("alice", "acme", null, null);

        assertThat(result.myRole).isNull();
        verify(memberRepo, never()).findByProjectAndUser(any(), any());
    }

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

    @Test
    void create_publishesCreateActivity_forTheNewProject() {
        User owner = new User();
        owner.id = UUID.randomUUID();

        ProjectDto dto = new ProjectDto();
        dto.name = "my-project";

        ProjectDto after = new ProjectDto();
        when(mapper.toDto(any(Project.class))).thenReturn(after);

        service.create(dto, owner);

        verify(activity).publish(eq(ActionType.Create), any(Project.class), eq(ProjectDto.class),
                isNull(), eq(after));
    }

    @Test
    void create_addsTheOwnerAsAnOwnerMember() {
        User owner = new User();
        owner.id = UUID.randomUUID();

        ProjectDto dto = new ProjectDto();
        dto.name = "my-project";

        service.create(dto, owner);

        ArgumentCaptor<ProjectUser> captor = ArgumentCaptor.forClass(ProjectUser.class);
        verify(memberRepo).persist(captor.capture());
        ProjectUser member = captor.getValue();
        assertThat(member.role).isEqualTo(ProjectRole.Owner);
        assertThat(member.user).isSameAs(owner);
        assertThat(member.project.name).isEqualTo("my-project");
    }

    @Test
    void create_publishesMemberCreatedActivity_forTheOwner() {
        User owner = new User();
        owner.id = UUID.randomUUID();

        ProjectDto dto = new ProjectDto();
        dto.name = "my-project";

        MemberDto memberAfter = new MemberDto();
        when(mapper.toDto(any(Project.class))).thenReturn(new ProjectDto());
        when(mapper.toDto(any(ProjectUser.class))).thenReturn(memberAfter);

        service.create(dto, owner);

        verify(activity).publish(eq(ActionType.Create), any(Project.class), eq(MemberDto.class),
                isNull(), eq(memberAfter));
    }

    @Test
    void update_publishesUpdateActivity_withBeforeAndAfter() {
        UUID id      = UUID.randomUUID();
        Project proj = projectWithId(id);

        ProjectDto dto = new ProjectDto();
        dto.id   = id;
        dto.name = "updated-name";

        ProjectDto before = new ProjectDto();
        ProjectDto after  = new ProjectDto();
        when(projectRepo.findByIdOptional(id)).thenReturn(Optional.of(proj));
        when(mapper.toDto(proj)).thenReturn(before, after);

        service.update(dto);

        verify(activity).publish(eq(ActionType.Update), eq(proj), eq(ProjectDto.class),
                eq(before), eq(after));
    }

    @Test
    void delete_publishesDeleteActivity_withBeforeSnapshot() {
        UUID id      = UUID.randomUUID();
        Project proj = projectWithId(id);

        ProjectDto before = new ProjectDto();
        when(projectRepo.findByIdOptional(id)).thenReturn(Optional.of(proj));
        when(mapper.toDto(proj)).thenReturn(before, new ProjectDto());

        service.delete(id);

        verify(activity).publish(eq(ActionType.Delete), eq(proj), eq(ProjectDto.class),
                eq(before), isNull());
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



