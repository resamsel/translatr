package com.translatr.criteria;

import jakarta.ws.rs.QueryParam;
import java.util.UUID;

public class MessageCriteria extends SearchCriteria {
    @QueryParam("projectId")  public UUID   projectId;
    @QueryParam("localeId")   public UUID   localeId;
    @QueryParam("localeIds")  public String localeIds;
    @QueryParam("keyId")      public UUID   keyId;
    @QueryParam("keyIds")     public String keyIds;
    @QueryParam("keyName")    public String keyName;
}
