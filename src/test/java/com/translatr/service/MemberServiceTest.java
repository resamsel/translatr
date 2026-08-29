package com.translatr.service;

import com.translatr.criteria.MemberCriteria;
import com.translatr.dto.MemberDto;
import com.translatr.mapper.DtoMapper;
import com.translatr.model.ActionType;
import com.translatr.model.Project;
import com.translatr.model.ProjectRole;
import com.translatr.model.ProjectUser;
import com.translatr.model.User;
import com.translatr.repository.ProjectRepository;
import com.translatr.repository.ProjectUserRepository;
import com.translatr.repository.UserRepository;
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
class MemberServiceTest {

    @Mock ProjectUserRepository memberRepo;
    @Mock ProjectRepository     projectRepo;
    @Mock UserRepository        userRepo;
    @Mock DtoMapper             mapper;
    @Mock ActivityLogger        activity;

    @InjectMocks MemberService service;

    @SuppressWarnings("unchecked")
    private String runFindAndCaptureQuery(MemberCriteria c) {
        PanacheQuery<ProjectUser> query = mock(PanacheQuery.class);
        when(memberRepo.find(anyString(), any(Object[].class))).thenReturn(query);
        when(query.count()).thenReturn(0L);
        when(query.page(anyInt(), anyInt())).thenReturn(query);
        when(query.list()).thenReturn(List.of());

        service.find(c);

        ArgumentCaptor<String> ql = ArgumentCaptor.forClass(String.class);
        verify(memberRepo).find(ql.capture(), any(Object[].class));
        return ql.getValue();
    }

    @Test
    void find_translatesProjectIdUserIdAndSearch() {
        MemberCriteria c = new MemberCriteria();
        c.projectId = UUID.randomUUID();
        c.userId    = UUID.randomUUID();
        c.search    = "ann";
        c.limit     = 20;

        String ql = runFindAndCaptureQuery(c);
        assertThat(ql).contains("pu.project.id = ");
        assertThat(ql).contains("pu.user.id = ");
        assertThat(ql).contains("lower(pu.user.username) LIKE ");
    }

    @Test
    void get_throwsNotFound_whenMemberMissing() {
        when(memberRepo.findByIdOptional(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(7L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void create_persistsMemberAndPublishesCreateActivity() {
        UUID projectId = UUID.randomUUID();
        UUID userId    = UUID.randomUUID();
        Project project = new Project("proj");
        project.id = projectId;
        User user = new User();
        user.id = userId;

        MemberDto dto = new MemberDto();
        dto.projectId = projectId;
        dto.userId    = userId;
        dto.role      = "Translator";

        MemberDto after = new MemberDto();
        when(projectRepo.findByIdOptional(projectId)).thenReturn(Optional.of(project));
        when(userRepo.findByIdOptional(userId)).thenReturn(Optional.of(user));
        when(mapper.toDto(any(ProjectUser.class))).thenReturn(after);

        service.create(dto);

        verify(memberRepo).persist(any(ProjectUser.class));
        verify(activity).publish(eq(ActionType.Create), eq(project), eq(MemberDto.class),
                isNull(), eq(after));
    }

    @Test
    void create_throwsNotFound_whenProjectMissing() {
        UUID projectId = UUID.randomUUID();
        MemberDto dto = new MemberDto();
        dto.projectId = projectId;
        dto.userId    = UUID.randomUUID();

        when(projectRepo.findByIdOptional(projectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(NotFoundException.class);
        verifyNoInteractions(activity);
    }

    @Test
    void update_appliesRoleAndPublishesUpdateActivity() {
        Project project = new Project("proj");
        ProjectUser member = new ProjectUser(ProjectRole.Translator);
        member.id      = 3L;
        member.project = project;

        MemberDto dto = new MemberDto();
        dto.id   = 3L;
        dto.role = "Owner";

        MemberDto before = new MemberDto();
        MemberDto after  = new MemberDto();
        when(memberRepo.findByIdOptional(3L)).thenReturn(Optional.of(member));
        when(mapper.toDto(member)).thenReturn(before, after);

        service.update(dto);

        assertThat(member.role).isEqualTo(ProjectRole.Owner);
        verify(activity).publish(eq(ActionType.Update), eq(project), eq(MemberDto.class),
                eq(before), eq(after));
    }

    @Test
    void delete_removesMemberAndPublishesDeleteActivity() {
        Project project = new Project("proj");
        ProjectUser member = new ProjectUser(ProjectRole.Translator);
        member.id      = 9L;
        member.project = project;

        MemberDto before = new MemberDto();
        when(memberRepo.findByIdOptional(9L)).thenReturn(Optional.of(member));
        when(mapper.toDto(member)).thenReturn(before);

        service.delete(9L);

        verify(memberRepo).delete(member);
        verify(activity).publish(eq(ActionType.Delete), eq(project), eq(MemberDto.class),
                eq(before), isNull());
    }
}
