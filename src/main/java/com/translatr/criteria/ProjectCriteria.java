package com.translatr.criteria;

import jakarta.ws.rs.QueryParam;
import java.util.UUID;

public class ProjectCriteria extends SearchCriteria {
    @QueryParam("ownerId")        public UUID   ownerId;
    @QueryParam("ownerUsername")  public String ownerUsername;
    @QueryParam("memberId")       public UUID   memberId;
    @QueryParam("name")           public String name;
}
