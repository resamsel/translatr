package com.translatr.criteria;

import jakarta.ws.rs.QueryParam;
import java.util.UUID;

public class AccessTokenCriteria extends SearchCriteria {
    @QueryParam("userId") public UUID userId;
}
