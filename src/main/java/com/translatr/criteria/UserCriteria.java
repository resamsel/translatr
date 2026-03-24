package com.translatr.criteria;

import jakarta.ws.rs.QueryParam;

public class UserCriteria extends SearchCriteria {
    @QueryParam("username") public String username;
    @QueryParam("email")    public String email;
}
