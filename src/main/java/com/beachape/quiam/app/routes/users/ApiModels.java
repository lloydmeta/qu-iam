package com.beachape.quiam.app.routes.users;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@RegisterForReflection // So that they can work with the native image
public final class ApiModels {
  private ApiModels() {} // Prevent instantiation

  public static record UpsertUserRequest(
      @NotBlank @Schema(example = "lloyd") String username,
      @NotBlank @Schema(example = "passw0rd") String password) {}

  public static record UpsertUserResponse(String message) {}

  public static record AuthenticationRequest(
      @NotBlank @Schema(example = "lloyd") String username,
      @NotBlank @Schema(example = "passw0rd") String password) {}

  public static record AuthenticationResponse(String username, String token) {}

  public static record ErrorResponse(String error) {}

  public static record EmptyResponse() {}

  public static record UserResponse(String username) {}
}
