package com.beachape.quiam.api.authentication;

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
@Priority(1)
public class LowestPriorityAuthenticationMechanism implements HttpAuthenticationMechanism {

  @Override
  public Uni<SecurityIdentity> authenticate(
      RoutingContext context, IdentityProviderManager identityProviderManager) {
    return Uni.createFrom()
        .failure(new NoAuthenticationMechanismFoundException("No authentication mechanism found"));
  }

  @Override
  public Uni<Boolean> sendChallenge(RoutingContext context) {
    return Uni.createFrom().item(true);
  }

  @Override
  public Set<Class<? extends AuthenticationRequest>> getCredentialTypes() {
    return Collections.singleton(TokenAuthenticationRequest.class);
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

  public static class NoAuthenticationMechanismFoundException extends SecurityException {
    public NoAuthenticationMechanismFoundException(String message) {
      super(message);
    }
  }
}
