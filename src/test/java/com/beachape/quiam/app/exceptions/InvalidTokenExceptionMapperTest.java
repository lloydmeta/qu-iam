package com.beachape.quiam.app.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import com.beachape.quiam.app.authentication.JwtIdentityProvider;
import com.beachape.quiam.app.routes.users.ApiModels;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NullAway")
final class InvalidTokenExceptionMapperTest {
  private final InvalidTokenExceptionMapper mapper = new InvalidTokenExceptionMapper();

  @Test
  void toResponse_shouldReturn401WithJsonError() {
    // Given
    JwtIdentityProvider.InvalidTokenException exception =
        new JwtIdentityProvider.InvalidTokenException("Test error message");

    // When
    Response response = mapper.toResponse(exception);

    // Then
    assertThat(response.getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
    assertThat(response.getMediaType()).isEqualTo(MediaType.APPLICATION_JSON_TYPE);

    ApiModels.ErrorResponse errorResponse = (ApiModels.ErrorResponse) response.getEntity();
    assertThat(errorResponse.error()).isEqualTo("Test error message");
  }
}
