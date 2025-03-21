package com.beachape.quiam.app.authentication;

import com.beachape.quiam.domain.jwt.JwtService;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.IdentityProvider;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.TokenAuthenticationRequest;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class JwtIdentityProvider implements IdentityProvider<TokenAuthenticationRequest> {
  public static final String JWT_TOKEN_KEY = "jwt_token";
  private final JwtService jwtService;

  @Inject
  public JwtIdentityProvider(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  public Class<TokenAuthenticationRequest> getRequestType() {
    return TokenAuthenticationRequest.class;
  }

  @Override
  public Uni<SecurityIdentity> authenticate(
      TokenAuthenticationRequest request, AuthenticationRequestContext context) {
    return Uni.createFrom()
        .item(
            () -> {
              String token = request.getToken().getToken();
              String userId = jwtService.validateToken(token);
              if (userId == null) {
                throw new InvalidTokenException("Invalid token");
              } else {
                return QuarkusSecurityIdentity.builder()
                    .setPrincipal(() -> userId)
                    .addAttribute(JWT_TOKEN_KEY, token)
                    .build();
              }
            });
  }

  public static class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
      super(message);
    }
  }
}
