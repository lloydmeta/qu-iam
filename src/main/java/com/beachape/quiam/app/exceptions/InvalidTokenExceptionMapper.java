package com.beachape.quiam.app.exceptions;

import com.beachape.quiam.app.authentication.JwtIdentityProvider.InvalidTokenException;
import com.beachape.quiam.app.routes.users.ApiModels;
import jakarta.annotation.Priority;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
@Priority(1)
public class InvalidTokenExceptionMapper implements ExceptionMapper<InvalidTokenException> {
  private static final Logger LOG = Logger.getLogger(InvalidTokenExceptionMapper.class);

  @Override
  public Response toResponse(InvalidTokenException exception) {
    LOG.info("Converting InvalidTokenException to Response: " + exception.getMessage());
    String message = exception.getMessage() != null ? exception.getMessage() : "Invalid token";
    return Response.status(Response.Status.UNAUTHORIZED)
        .type(MediaType.APPLICATION_JSON)
        .entity(new ApiModels.ErrorResponse(message))
        .build();
  }
}
