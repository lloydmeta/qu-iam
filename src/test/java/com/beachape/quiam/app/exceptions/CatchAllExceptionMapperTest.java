package com.beachape.quiam.app.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import com.beachape.quiam.app.routes.users.ApiModels;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

final class CatchAllExceptionMapperTest {
  private final CatchAllExceptionMapper mapper = new CatchAllExceptionMapper();

  @Test
  void toResponse_shouldReturn500WithJsonError_whenExceptionHasMessage() {
    // Given
    Exception exception = new RuntimeException("Test error message");

    // When
    Response response = mapper.toResponse(exception);

    // Then
    assertThat(response.getStatus())
        .isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    assertThat(response.getMediaType()).isEqualTo(MediaType.APPLICATION_JSON_TYPE);

    ApiModels.ErrorResponse errorResponse = (ApiModels.ErrorResponse) response.getEntity();
    assertThat(errorResponse.error()).isEqualTo("Test error message");
  }

  @Test
  void toResponse_shouldReturn500WithDefaultMessage_whenExceptionHasNoMessage() {
    // Given
    Exception exception = new RuntimeException();

    // When
    Response response = mapper.toResponse(exception);

    // Then
    assertThat(response.getStatus())
        .isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    assertThat(response.getMediaType()).isEqualTo(MediaType.APPLICATION_JSON_TYPE);

    ApiModels.ErrorResponse errorResponse = (ApiModels.ErrorResponse) response.getEntity();
    assertThat(errorResponse.error()).isEqualTo("Server side internal exception");
  }

  @Test
  void toResponse_shouldHandleErrorClass() {
    // Given
    Error error = new Error("Test error");

    // When
    Response response = mapper.toResponse(error);

    // Then
    assertThat(response.getStatus())
        .isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    assertThat(response.getMediaType()).isEqualTo(MediaType.APPLICATION_JSON_TYPE);

    ApiModels.ErrorResponse errorResponse = (ApiModels.ErrorResponse) response.getEntity();
    assertThat(errorResponse.error()).isEqualTo("Test error");
  }
}
