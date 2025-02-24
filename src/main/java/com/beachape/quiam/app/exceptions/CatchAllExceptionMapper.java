package com.beachape.quiam.app.exceptions;

import com.beachape.quiam.app.routes.users.DataTransferObjects;
import jakarta.annotation.Priority;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(1)
public class CatchAllExceptionMapper implements ExceptionMapper<Throwable> {
  @Override
  public Response toResponse(Throwable exception) {
    String message =
        exception.getMessage() != null ? exception.getMessage() : "Server side internal exception";

    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
        .type(MediaType.APPLICATION_JSON)
        .entity(new DataTransferObjects.ErrorResponse(message))
        .build();
  }
}
