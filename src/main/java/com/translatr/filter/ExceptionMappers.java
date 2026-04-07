package com.translatr.filter;

import com.translatr.dto.ErrorResponse;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

public class ExceptionMappers {

    @Provider
    public static class NotFoundMapper implements ExceptionMapper<NotFoundException> {
        @Override
        public Response toResponse(NotFoundException e) {
            return Response.status(404)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ErrorResponse(404, e.getMessage() != null ? e.getMessage() : "Not found"))
                    .build();
        }
    }

    @Provider
    public static class ForbiddenMapper implements ExceptionMapper<io.quarkus.security.ForbiddenException> {
        @Override
        public Response toResponse(io.quarkus.security.ForbiddenException e) {
            return Response.status(403)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ErrorResponse(403, "Forbidden"))
                    .build();
        }
    }

    // NOTE: UnauthorizedException and the generic Exception mapper are intentionally absent.
    //
    // Registering an ExceptionMapper<UnauthorizedException> intercepts the exception
    // *before* Quarkus OIDC can call HttpAuthenticationMechanism.sendChallenge(), which
    // is the mechanism that issues the browser redirect to Keycloak (hybrid mode) or
    // returns a proper 401 for AJAX/API requests.
    //
    // Without a mapper, Quarkus OIDC handles it natively:
    //   - browser request (Accept: text/html)  → 302 redirect to Keycloak
    //   - AJAX / API request                   → 401 Unauthorized
}
