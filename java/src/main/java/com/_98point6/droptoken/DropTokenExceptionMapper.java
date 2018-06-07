package com._98point6.droptoken;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 *
 */
public class DropTokenExceptionMapper implements ExceptionMapper<RuntimeException> {
    private static final Logger logger = LoggerFactory.getLogger(DropTokenExceptionMapper.class);

    public Response toResponse(RuntimeException e) {
        logger.error("Unhandled exception.", e);

        // 400
        if (e instanceof BadRequestException) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        // 404
        if (e instanceof NotFoundException) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // 409
        if (e instanceof ConflictException) {
            return Response.status(Response.Status.CONFLICT).build();
        }

        // 410
        if (e instanceof GoneException) {
            return Response.status(Response.Status.GONE).build();
        }

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
}
