package com.translatr.criteria;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;

public class SearchCriteria {
    @QueryParam("search")   public String search;
    @QueryParam("offset")   @DefaultValue("0")  public int offset;
    @QueryParam("limit")    @DefaultValue("20") public int limit;
    @QueryParam("order")    public String order;
    @QueryParam("fetch")    public String fetch;
}
