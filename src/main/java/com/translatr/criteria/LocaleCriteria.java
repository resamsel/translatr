package com.translatr.criteria;

import jakarta.ws.rs.QueryParam;
import java.util.UUID;

public class LocaleCriteria extends SearchCriteria {
    @QueryParam("projectId")  public UUID    projectId;
    @QueryParam("keyId")      public UUID    keyId;
    @QueryParam("missing")    public Boolean missing;
    @QueryParam("localeName") public String  localeName;
}
