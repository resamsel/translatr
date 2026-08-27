package com.translatr.service;

import com.translatr.criteria.MemberCriteria;
import com.translatr.dto.MemberDto;
import com.translatr.dto.PagedList;
import com.translatr.mapper.DtoMapper;
import com.translatr.model.ActionType;
import com.translatr.model.ProjectRole;
import com.translatr.model.ProjectUser;
import com.translatr.repository.ProjectRepository;
import com.translatr.repository.ProjectUserRepository;
import com.translatr.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.util.stream.Collectors;

@ApplicationScoped
public class MemberService {

    private final ProjectUserRepository memberRepo;
    private final ProjectRepository     projectRepo;
    private final UserRepository        userRepo;
    private final DtoMapper             mapper;
    private final ActivityLogger        activity;

    @Inject
    public MemberService(ProjectUserRepository memberRepo, ProjectRepository projectRepo,
                         UserRepository userRepo, DtoMapper mapper, ActivityLogger activity) {
        this.memberRepo  = memberRepo;
        this.projectRepo = projectRepo;
        this.userRepo    = userRepo;
        this.mapper      = mapper;
        this.activity    = activity;
    }

    public PagedList<MemberDto> find(MemberCriteria c) {
        var query = memberRepo.find(
                "(?1 IS NULL OR project.id = ?1) AND (?2 IS NULL OR user.id = ?2) ORDER BY whenCreated DESC",
                c.projectId, c.userId);
        long total = query.count();
        var list = query.page(c.offset / Math.max(c.limit, 1), c.limit).list()
                .stream().map(mapper::toDto).collect(Collectors.toList());
        return new PagedList<>(list, total, c.offset, c.limit);
    }

    public MemberDto get(Long id) {
        return mapper.toDto(memberRepo.findByIdOptional(id).orElseThrow(NotFoundException::new));
    }

    @Transactional
    public MemberDto create(MemberDto dto) {
        var project = projectRepo.findByIdOptional(dto.projectId).orElseThrow(NotFoundException::new);
        var user    = userRepo.findByIdOptional(dto.userId).orElseThrow(NotFoundException::new);
        var member  = new ProjectUser(dto.role != null ? ProjectRole.valueOf(dto.role) : ProjectRole.Translator);
        member.project = project;
        member.user    = user;
        memberRepo.persist(member);
        MemberDto after = mapper.toDto(member);
        activity.publish(ActionType.Create, project, MemberDto.class, null, after);
        return after;
    }

    @Transactional
    public MemberDto update(MemberDto dto) {
        var member = memberRepo.findByIdOptional(dto.id).orElseThrow(NotFoundException::new);
        MemberDto before = mapper.toDto(member);
        if (dto.role != null) member.role = ProjectRole.valueOf(dto.role);
        MemberDto after = mapper.toDto(member);
        activity.publish(ActionType.Update, member.project, MemberDto.class, before, after);
        return after;
    }

    @Transactional
    public MemberDto delete(Long id) {
        var member = memberRepo.findByIdOptional(id).orElseThrow(NotFoundException::new);
        MemberDto before = mapper.toDto(member);
        var project = member.project;
        memberRepo.delete(member);
        activity.publish(ActionType.Delete, project, MemberDto.class, before, null);
        return before;
    }
}
