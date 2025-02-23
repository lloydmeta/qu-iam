package com.beachape.quiam.api.exceptions;

import static org.junit.jupiter.api.Assertions.*;

import com.beachape.quiam.api.authentication.LowestPriorityAuthenticationMechanism.*;
import com.beachape.quiam.api.users.DataTransferObjects;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

class NoAuthenticationMechanismFoundExceptionMapperTest {
  private final NoAuthenticationMechanismFoundExceptionMapper mapper =
      new NoAuthenticationMechanismFoundExceptionMapper();

  @Test
  void toResponse_shouldReturn401WithJsonError() {
    // Given
    NoAuthenticationMechanismFoundException exception =
        new NoAuthenticationMechanismFoundException("Invalid session");

    // When
    Response response = mapper.toResponse(exception);

    // Then
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());

    DataTransferObjects.ErrorResponse errorResponse =
        (DataTransferObjects.ErrorResponse) response.getEntity();
    assertEquals("Invalid session", errorResponse.error());
  }

  @Test
  void toResponse_shouldReturn401WithJsonError_whenExceptionHasMessage() {
    // Given
    NoAuthenticationMechanismFoundException exception =
        new NoAuthenticationMechanismFoundException("Custom error message");

    // When
    Response response = mapper.toResponse(exception);

    // Then
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());

    DataTransferObjects.ErrorResponse errorResponse =
        (DataTransferObjects.ErrorResponse) response.getEntity();
    assertEquals("Custom error message", errorResponse.error());
  }
}
