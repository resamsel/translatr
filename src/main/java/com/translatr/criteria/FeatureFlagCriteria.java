package com.translatr.criteria;

import jakarta.ws.rs.QueryParam;
import java.util.UUID;

public class FeatureFlagCriteria extends SearchCriteria {
    @QueryParam("userId")  public UUID   userId;
    @QueryParam("feature") public String feature;
}

