package com.translatr.filter;

import com.translatr.dto.ErrorResponse;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    @Provider
    public static class UnauthorizedMapper implements ExceptionMapper<io.quarkus.security.UnauthorizedException> {
        @Override
        public Response toResponse(io.quarkus.security.UnauthorizedException e) {
            return Response.status(401)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ErrorResponse(401, "Unauthorized"))
                    .build();
        }
    }

    @Provider
    public static class GenericMapper implements ExceptionMapper<Exception> {
        private static final Logger LOG = LoggerFactory.getLogger(GenericMapper.class);

        @Override
        public Response toResponse(Exception e) {
            if (e instanceof NotFoundException)               return new NotFoundMapper().toResponse((NotFoundException) e);
            if (e instanceof io.quarkus.security.ForbiddenException)    return new ForbiddenMapper().toResponse((io.quarkus.security.ForbiddenException) e);
            if (e instanceof io.quarkus.security.UnauthorizedException) return new UnauthorizedMapper().toResponse((io.quarkus.security.UnauthorizedException) e);
            LOG.error("Unhandled exception", e);
            return Response.status(500)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ErrorResponse(500, "Internal server error"))
                    .build();
        }
    }
}
