package com.beachape.quiam.app.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import com.beachape.quiam.app.authentication.LowestPriorityAuthenticationMechanism.NoAuthenticationMechanismFoundException;
import com.beachape.quiam.app.routes.users.ApiModels;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

final class NoAuthenticationMechanismFoundExceptionMapperTest {
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
    assertThat(response.getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
    assertThat(response.getMediaType()).isEqualTo(MediaType.APPLICATION_JSON_TYPE);

    ApiModels.ErrorResponse errorResponse = (ApiModels.ErrorResponse) response.getEntity();
    assertThat(errorResponse.error()).isEqualTo("Invalid session");
  }

  @Test
  void toResponse_shouldReturn401WithJsonError_whenExceptionHasMessage() {
    // Given
    NoAuthenticationMechanismFoundException exception =
        new NoAuthenticationMechanismFoundException("Custom error message");

    // When
    Response response = mapper.toResponse(exception);

    // Then
    assertThat(response.getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
    assertThat(response.getMediaType()).isEqualTo(MediaType.APPLICATION_JSON_TYPE);

    ApiModels.ErrorResponse errorResponse = (ApiModels.ErrorResponse) response.getEntity();
    assertThat(errorResponse.error()).isEqualTo("Custom error message");
  }
}
