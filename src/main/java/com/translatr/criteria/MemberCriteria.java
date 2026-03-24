package com.translatr.criteria;

import jakarta.ws.rs.QueryParam;
import java.util.UUID;

public class MemberCriteria extends SearchCriteria {
    @QueryParam("projectId") public UUID projectId;
    @QueryParam("userId")    public UUID userId;
}
