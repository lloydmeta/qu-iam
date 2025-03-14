package com.beachape.quiam.app.routes.users;

import com.beachape.quiam.domain.users.UsersService.UpsertUser;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.mapstruct.Mapper;

@RegisterForReflection // So that they can work with the native image
public final class ApiModels {
  private ApiModels() {} // Prevent instantiation

  @Builder
  public static record UpsertUserRequest(
      @NotBlank @Schema(examples = {"lloyd"}) String username,
      @NotBlank @Schema(examples = {"passw0rd"}) String password) {}

  public static record UpsertUserResponse(String message) {}

  @Builder
  public static record AuthenticationRequest(
      @NotBlank @Schema(examples = {"lloyd"}) String username,
      @NotBlank @Schema(examples = {"passw0rd"}) String password) {}

  @Builder
  public static record AuthenticationResponse(String username, String token) {}

  public static record ErrorResponse(String error) {}

  public static record EmptyResponse() {}

  public static record UserResponse(String username) {}

  public static final class Mappers {

    // This is implemented by MapStruct and wired in
    @Mapper(componentModel = "cdi")
    public static interface UpsertUserRequestMapper {
      UpsertUser fromApiModel(@NotNull UpsertUserRequest apiModel);
    }
  }
}
