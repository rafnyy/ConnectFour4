package com._98point6.droptoken;

import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class DropTokenExceptionMapperTest {

    private DropTokenExceptionMapper dropTokenExceptionMapper = new DropTokenExceptionMapper();
    @Test
    public void toResponse400() {
        Response response = dropTokenExceptionMapper.toResponse(new BadRequestException());
        assertThat(response.getStatus(), is((equalTo(HttpStatus.BAD_REQUEST_400))));
    }

    @Test
    public void toResponse404() {
        Response response = dropTokenExceptionMapper.toResponse(new NotFoundException());
        assertThat(response.getStatus(), is((equalTo(HttpStatus.NOT_FOUND_404))));
    }

    @Test
    public void toResponse409() {
        Response response = dropTokenExceptionMapper.toResponse(new ConflictException());
        assertThat(response.getStatus(), is((equalTo(HttpStatus.CONFLICT_409))));
    }

    @Test
    public void toResponse410() {
        Response response = dropTokenExceptionMapper.toResponse(new GoneException());
        assertThat(response.getStatus(), is((equalTo(HttpStatus.GONE_410))));
    }

    @Test
    public void toResponse500() {
        Response response = dropTokenExceptionMapper.toResponse(new RuntimeException());
        assertThat(response.getStatus(), is((equalTo(HttpStatus.INTERNAL_SERVER_ERROR_500))));
    }
}