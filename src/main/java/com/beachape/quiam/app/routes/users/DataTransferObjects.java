package com.beachape.quiam.app.routes.users;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;

@RegisterForReflection // So that they can work with the native image
public final class DataTransferObjects {
  private DataTransferObjects() {} // Prevent instantiation

  public static record UpsertUserRequest(@NotBlank String username, @NotBlank String password) {}

  public static record UpsertUserResponse(String message) {}

  public static record AuthenticationRequest(
      @NotBlank String username, @NotBlank String password) {}

  public static record AuthenticationResponse(String username, String token) {}

  public static record ErrorResponse(String error) {}

  public static record UserResponse(String username) {}
}
