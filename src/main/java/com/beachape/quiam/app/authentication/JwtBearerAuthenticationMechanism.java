package com.beachape.quiam.app.authentication;

import io.quarkus.security.credential.TokenCredential;
import io.quarkus.security.identity.IdentityProviderManager;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.AuthenticationRequest;
import io.quarkus.security.identity.request.TokenAuthenticationRequest;
import io.quarkus.vertx.http.runtime.security.ChallengeData;
import io.quarkus.vertx.http.runtime.security.HttpAuthenticationMechanism;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import java.util.Collections;
import java.util.Set;

@ApplicationScoped
@Priority(3)
public class JwtBearerAuthenticationMechanism implements HttpAuthenticationMechanism {
  private static final String BEARER_PREFIX = "Bearer ";

  @Override
  public Uni<SecurityIdentity> authenticate(
      RoutingContext context, IdentityProviderManager identityProviderManager) {
    String authHeader = context.request().getHeader(HttpHeaders.AUTHORIZATION);

    if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
      return Uni.createFrom().nullItem();
    }

    String token = authHeader.substring(BEARER_PREFIX.length());
    if (token.isEmpty()) {
      return Uni.createFrom().nullItem();
    }
    TokenAuthenticationRequest request =
        new TokenAuthenticationRequest(new TokenCredential(token, "JWT"));
    return identityProviderManager.authenticate(request);
  }

  @Override
  public Uni<ChallengeData> getChallenge(RoutingContext context) {
    ChallengeData challenge =
        new ChallengeData(
            Response.Status.UNAUTHORIZED.getStatusCode(),
            HttpHeaders.WWW_AUTHENTICATE,
            "Bearer realm=\"beachape-api\"");

    return Uni.createFrom().item(challenge);
  }

  @Override
  public Set<Class<? extends AuthenticationRequest>> getCredentialTypes() {
    return Collections.singleton(TokenAuthenticationRequest.class);
  }
}
