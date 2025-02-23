package com.beachape.quiam.api.authentication;

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
@Priority(2)
public class JwtCookieAuthenticationMechanism implements HttpAuthenticationMechanism {

  @Override
  public Uni<SecurityIdentity> authenticate(
      RoutingContext context, IdentityProviderManager identityProviderManager) {
    var sessionCookie = context.request().getCookie("session");
    if (sessionCookie == null || sessionCookie.getValue() == null) {
      return Uni.createFrom().nullItem();
    }

    String token = sessionCookie.getValue();
    TokenAuthenticationRequest request =
        new TokenAuthenticationRequest(new TokenCredential(token, "JWT"));
    return identityProviderManager.authenticate(request);
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
            "cookie; cookie-name=\"session\"");

    return Uni.createFrom().item(challenge);
  }
}
