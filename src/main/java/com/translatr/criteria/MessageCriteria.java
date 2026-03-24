package com.translatr.criteria;

import jakarta.ws.rs.QueryParam;
import java.util.UUID;

public class MessageCriteria extends SearchCriteria {
    @QueryParam("projectId")  public UUID   projectId;
    @QueryParam("localeId")   public UUID   localeId;
    @QueryParam("keyId")      public UUID   keyId;
    @QueryParam("keyName")    public String keyName;
}
