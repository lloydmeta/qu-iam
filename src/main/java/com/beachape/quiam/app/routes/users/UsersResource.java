package com.beachape.quiam.app.routes.users;

import com.beachape.quiam.app.authentication.JwtIdentityProvider;
import com.beachape.quiam.app.routes.users.ApiModels.AuthenticationRequest;
import com.beachape.quiam.app.routes.users.ApiModels.AuthenticationResponse;
import com.beachape.quiam.app.routes.users.ApiModels.EmptyResponse;
import com.beachape.quiam.app.routes.users.ApiModels.ErrorResponse;
import com.beachape.quiam.app.routes.users.ApiModels.Mappers;
import com.beachape.quiam.app.routes.users.ApiModels.UpsertUserRequest;
import com.beachape.quiam.app.routes.users.ApiModels.UpsertUserResponse;
import com.beachape.quiam.app.routes.users.ApiModels.UserResponse;
import com.beachape.quiam.domain.jwt.JwtService;
import com.beachape.quiam.domain.users.UsersService;
import com.beachape.quiam.domain.users.UsersService.UpsertUser;
import com.beachape.quiam.domain.users.UsersService.User;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.common.annotation.RunOnVirtualThread;
import io.vertx.core.http.HttpServerRequest;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponseSchema;

@Path("/api/users")
@RunOnVirtualThread
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UsersResource {
  private final UsersService usersService;
  private final JwtService jwtService;
  private final Mappers.UpsertUserRequestMapper upsertUserRequestMapper;

  @Inject
  public UsersResource(
      UsersService usersService,
      JwtService jwtService,
      Mappers.UpsertUserRequestMapper upsertUserRequestMapper) {
    this.upsertUserRequestMapper = upsertUserRequestMapper;
    this.usersService = usersService;
    this.jwtService = jwtService;
  }

  @POST
  @Path("/_upsert")
  public UpsertUserResponse upsertUser(@Valid @NotNull UpsertUserRequest request) {
    UpsertUser domainUser = upsertUserRequestMapper.fromApiModel(request);
    usersService.upsert(domainUser);
    return new UpsertUserResponse("User upserted successfully");
  }

  @POST
  @Path("/_login")
  @APIResponseSchema(value = AuthenticationResponse.class)
  @PermitAll
  public Response login(
      @Valid @NotNull AuthenticationRequest request, @Context HttpServerRequest serverRequest) {
    try {
      User user = usersService.authenticate(request.username(), request.password());
      String token = jwtService.createToken(user.name());

      NewCookie sessionCookie =
          new NewCookie.Builder("session")
              .value(token)
              .path("/")
              .httpOnly(true)
              .secure(serverRequest.isSSL())
              .build();

      return Response.ok(new AuthenticationResponse(user.name(), token))
          .cookie(sessionCookie)
          .build();
    } catch (UsersService.NoSuchUser e) {
      throw new WebApplicationException(
          Response.status(Response.Status.NOT_FOUND)
              .entity(new ErrorResponse("User not found"))
              .build());
    } catch (UsersService.InvalidPassword e) {
      throw new WebApplicationException(
          Response.status(Response.Status.UNAUTHORIZED)
              .entity(new ErrorResponse("Invalid password"))
              .build());
    }
  }

  @POST
  @Path("/_logout")
  @Authenticated
  @Consumes({MediaType.APPLICATION_JSON, MediaType.WILDCARD})
  @APIResponseSchema(value = EmptyResponse.class)
  public Response logout(
      @Context SecurityIdentity securityIdentity, @Context HttpServerRequest serverRequest) {
    try {
      String token = securityIdentity.getAttribute(JwtIdentityProvider.JWT_TOKEN_KEY);
      jwtService.invalidateToken(token);

      NewCookie clearCookie =
          new NewCookie.Builder("session")
              .value("")
              .path("/")
              .maxAge(0)
              .httpOnly(true)
              .secure(serverRequest.isSSL())
              .build();
      return Response.ok().entity(new EmptyResponse()).cookie(clearCookie).build();
    } catch (JwtService.TokenValidationException e) {
      throw new WebApplicationException(
          Response.status(Response.Status.UNAUTHORIZED)
              .entity(new ErrorResponse("Invalid session token"))
              .build());
    }
  }

  @GET
  @Path("/me")
  @Authenticated
  public UserResponse getUser(@Context SecurityIdentity securityIdentity) {
    String username = securityIdentity.getPrincipal().getName();
    return new UserResponse(username);
  }
}
