package com.translatr.controller;

import com.translatr.criteria.MemberCriteria;
import com.translatr.dto.MemberDto;
import com.translatr.dto.PagedList;
import com.translatr.dto.PagedMemberList;
import com.translatr.generated.api.MembersApi;
import com.translatr.service.MemberService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import java.util.UUID;

public class MemberResource implements MembersApi {

    private final MemberService memberService;

    @Inject
    public MemberResource(MemberService memberService) {
        this.memberService = memberService;
    }

    @Override
    @PermitAll
    public PagedMemberList findMembersByProject(UUID projectId, String search, Integer offset, Integer limit,
                                                 String order, String fetch, UUID userId) {
        var criteria = toCriteria(search, offset, limit, order, fetch, projectId, userId);
        return toPagedDto(memberService.find(criteria));
    }

    @Override
    @PermitAll
    public PagedMemberList findMembersByProjectLegacy(UUID projectId, String search, Integer offset, Integer limit,
                                                       String order, String fetch, UUID userId) {
        var criteria = toCriteria(search, offset, limit, order, fetch, projectId, userId);
        return toPagedDto(memberService.find(criteria));
    }

    @Override
    @PermitAll
    public MemberDto getMember(Long id) {
        return memberService.get(id);
    }

    @Override
    @Authenticated
    public MemberDto createMember(MemberDto memberDto) {
        return memberService.create(memberDto);
    }

    @Override
    @Authenticated
    public MemberDto updateMember(MemberDto memberDto) {
        return memberService.update(memberDto);
    }

    @Override
    @Authenticated
    public MemberDto deleteMember(Long id) {
        return memberService.delete(id);
    }

    static MemberCriteria toCriteria(String search, Integer offset, Integer limit, String order, String fetch,
                                      UUID projectId, UUID userId) {
        MemberCriteria c = new MemberCriteria();
        c.search    = search;
        c.offset    = offset;
        c.limit     = limit;
        c.order     = order;
        c.fetch     = fetch;
        c.projectId = projectId;
        c.userId    = userId;
        return c;
    }

    private static PagedMemberList toPagedDto(PagedList<MemberDto> src) {
        return new PagedMemberList(src.total, src.offset, src.limit, src.hasNext, src.hasPrev, src.list);
    }
}
