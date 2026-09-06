package com.translatr.controller;

import com.translatr.dto.HealthStatus;
import com.translatr.generated.api.HealthApi;
import jakarta.annotation.security.PermitAll;

public class HealthResource implements HealthApi {

    @Override
    @PermitAll
    public HealthStatus getHealth() {
        return new HealthStatus().status("ok");
    }
}
