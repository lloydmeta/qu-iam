package com.beachape.quiam.api.exceptions;

import com.beachape.quiam.api.authentication.LowestPriorityAuthenticationMechanism;
import com.beachape.quiam.api.routes.users.DataTransferObjects;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.Optional;

@Provider
public class NoAuthenticationMechanismFoundExceptionMapper
    implements ExceptionMapper<
        LowestPriorityAuthenticationMechanism.NoAuthenticationMechanismFoundException> {

  @Override
  public Response toResponse(
      LowestPriorityAuthenticationMechanism.NoAuthenticationMechanismFoundException exception) {
    String message =
        Optional.ofNullable(exception.getMessage()).orElse("No Authentication Mechanism Found");
    return Response.status(Response.Status.UNAUTHORIZED)
        .type(MediaType.APPLICATION_JSON)
        .entity(new DataTransferObjects.ErrorResponse(message))
        .build();
  }
}
