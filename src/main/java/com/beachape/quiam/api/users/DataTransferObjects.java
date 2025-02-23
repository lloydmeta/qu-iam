package com.beachape.quiam.api.users;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection // So that they can work with the native image
public final class DataTransferObjects {
  private DataTransferObjects() {} // Prevent instantiation

  public static record UpsertUserRequest(String username, String password) {}

  public static record UpsertUserResponse(String message) {}

  public static record AuthenticationRequest(String username, String password) {}

  public static record AuthenticationResponse(String username, String token) {}

  public static record ErrorResponse(String error) {}

  public record UserResponse(String username) {}
}
