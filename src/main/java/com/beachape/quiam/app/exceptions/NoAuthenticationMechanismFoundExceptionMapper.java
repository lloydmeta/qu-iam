package com.beachape.quiam.app.exceptions;

import static java.util.Objects.requireNonNullElse;

import com.beachape.quiam.app.authentication.LowestPriorityAuthenticationMechanism;
import com.beachape.quiam.app.routes.users.ApiModels;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class NoAuthenticationMechanismFoundExceptionMapper
    implements ExceptionMapper<
        LowestPriorityAuthenticationMechanism.NoAuthenticationMechanismFoundException> {

  @Override
  public Response toResponse(
      LowestPriorityAuthenticationMechanism.NoAuthenticationMechanismFoundException exception) {
    String message =
        requireNonNullElse(exception.getMessage(), "No Authentication Mechanism Found");
    return Response.status(Response.Status.UNAUTHORIZED)
        .type(MediaType.APPLICATION_JSON)
        .entity(new ApiModels.ErrorResponse(message))
        .build();
  }
}
