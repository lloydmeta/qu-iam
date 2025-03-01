package com.beachape.quiam.app.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.beachape.quiam.app.routes.users.ApiModels;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

class CatchAllExceptionMapperTest {
  private final CatchAllExceptionMapper mapper = new CatchAllExceptionMapper();

  @Test
  void toResponse_shouldReturn500WithJsonError_whenExceptionHasMessage() {
    // Given
    Exception exception = new RuntimeException("Test error message");

    // When
    Response response = mapper.toResponse(exception);

    // Then
    assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());

    ApiModels.ErrorResponse errorResponse = (ApiModels.ErrorResponse) response.getEntity();
    assertEquals("Test error message", errorResponse.error());
  }

  @Test
  void toResponse_shouldReturn500WithDefaultMessage_whenExceptionHasNoMessage() {
    // Given
    Exception exception = new RuntimeException();

    // When
    Response response = mapper.toResponse(exception);

    // Then
    assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());

    ApiModels.ErrorResponse errorResponse = (ApiModels.ErrorResponse) response.getEntity();
    assertEquals("Server side internal exception", errorResponse.error());
  }

  @Test
  void toResponse_shouldHandleErrorClass() {
    // Given
    Error error = new Error("Test error");

    // When
    Response response = mapper.toResponse(error);

    // Then
    assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());

    ApiModels.ErrorResponse errorResponse = (ApiModels.ErrorResponse) response.getEntity();
    assertEquals("Test error", errorResponse.error());
  }
}
