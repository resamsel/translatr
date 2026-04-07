package com.translatr.controller;

import com.translatr.criteria.MemberCriteria;
import com.translatr.dto.MemberDto;
import com.translatr.dto.PagedList;
import com.translatr.service.MemberService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.UUID;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MemberResource {

    @Inject MemberService memberService;

    /** GET /api/project/{projectId}/members */
    @GET
    @Path("/project/{projectId}/members")
    @PermitAll
    public PagedList<MemberDto> findByProject(@PathParam("projectId") UUID projectId,
                                              @BeanParam MemberCriteria criteria) {
        criteria.projectId = projectId;
        return memberService.find(criteria);
    }

    /** GET /api/members/{projectId}  (legacy path, same behaviour) */
    @GET
    @Path("/members/{projectId}")
    @PermitAll
    public PagedList<MemberDto> findByProjectLegacy(@PathParam("projectId") UUID projectId,
                                                    @BeanParam MemberCriteria criteria) {
        criteria.projectId = projectId;
        return memberService.find(criteria);
    }

    @GET
    @Path("/member/{id}")
    @PermitAll
    public MemberDto get(@PathParam("id") Long id) {
        return memberService.get(id);
    }

    @POST
    @Path("/member")
    @Authenticated
    public MemberDto create(MemberDto dto) {
        return memberService.create(dto);
    }

    @PUT
    @Path("/member")
    @Authenticated
    public MemberDto update(MemberDto dto) {
        return memberService.update(dto);
    }

    @DELETE
    @Path("/member/{id}")
    @Authenticated
    public MemberDto delete(@PathParam("id") Long id) {
        return memberService.delete(id);
    }
}

