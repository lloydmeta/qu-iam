package com.beachape.quiam.api.exceptions;

import static org.junit.jupiter.api.Assertions.*;

import com.beachape.quiam.api.authentication.JwtIdentityProvider;
import com.beachape.quiam.api.users.DataTransferObjects;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NullAway")
class InvalidTokenExceptionMapperTest {
  private final InvalidTokenExceptionMapper mapper = new InvalidTokenExceptionMapper();

  @Test
  void toResponse_shouldReturn401WithJsonError() {
    // Given
    JwtIdentityProvider.InvalidTokenException exception =
        new JwtIdentityProvider.InvalidTokenException("Test error message", null);

    // When
    Response response = mapper.toResponse(exception);

    // Then
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());

    DataTransferObjects.ErrorResponse errorResponse =
        (DataTransferObjects.ErrorResponse) response.getEntity();
    assertEquals("Test error message", errorResponse.error());
  }

  @Test
  void toResponse_shouldReturn401WithJsonError_whenExceptionHasNoMessage() {
    // Given
    JwtIdentityProvider.InvalidTokenException exception =
        new JwtIdentityProvider.InvalidTokenException(null, new Exception());

    // When
    Response response = mapper.toResponse(exception);

    // Then
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());

    DataTransferObjects.ErrorResponse errorResponse =
        (DataTransferObjects.ErrorResponse) response.getEntity();
    assertEquals("Invalid token", errorResponse.error());
  }

  @Test
  void toResponse_shouldReturn401WithJsonError_whenExceptionHasCause() {
    // Given
    Exception cause = new Exception("Original error");
    JwtIdentityProvider.InvalidTokenException exception =
        new JwtIdentityProvider.InvalidTokenException("Test error message", cause);

    // When
    Response response = mapper.toResponse(exception);

    // Then
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());

    DataTransferObjects.ErrorResponse errorResponse =
        (DataTransferObjects.ErrorResponse) response.getEntity();
    assertEquals("Test error message", errorResponse.error());
  }
}
