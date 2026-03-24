package com.translatr.criteria;

import jakarta.ws.rs.QueryParam;
import java.util.UUID;

public class KeyCriteria extends SearchCriteria {
    @QueryParam("projectId")  public UUID    projectId;
    @QueryParam("localeId")   public UUID    localeId;
    @QueryParam("missing")    public Boolean missing;
}
