package com.beachape.quiam.app.authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import io.quarkus.security.identity.IdentityProviderManager;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.TokenAuthenticationRequest;
import io.quarkus.vertx.http.runtime.security.ChallengeData;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@SuppressWarnings("NullAway")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
final class JwtBearerAuthenticationMechanismTest {
  @Mock private IdentityProviderManager identityProviderManager;

  @Mock private RoutingContext routingContext;

  @Mock private HttpServerRequest request;

  @Mock private SecurityIdentity securityIdentity;

  private JwtBearerAuthenticationMechanism mechanism;

  @BeforeEach
  void setUp() {
    mechanism = new JwtBearerAuthenticationMechanism();
    when(routingContext.request()).thenReturn(request);
  }

  @Test
  void authenticate_shouldReturnNullUni_whenNoAuthorizationHeader() {
    // Given
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

    // When
    Uni<SecurityIdentity> result = mechanism.authenticate(routingContext, identityProviderManager);

    // Then
    assertThat(result.await().indefinitely()).isNull();
    verify(identityProviderManager, never()).authenticate(any());
  }

  @Test
  void authenticate_shouldReturnNullUni_whenNotBearerToken() {
    // Given
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Basic abc123");

    // When
    Uni<SecurityIdentity> result = mechanism.authenticate(routingContext, identityProviderManager);

    // Then
    assertThat(result.await().indefinitely()).isNull();
    verify(identityProviderManager, never()).authenticate(any());
  }

  @Test
  void authenticate_shouldReturnNullUni_whenEmptyToken() {
    // Given
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer ");

    // When
    Uni<SecurityIdentity> result = mechanism.authenticate(routingContext, identityProviderManager);

    // Then
    assertThat(result.await().indefinitely()).isNull();
    verify(identityProviderManager, never()).authenticate(any());
  }

  @Test
  void authenticate_shouldDelegateToIdentityProvider_whenValidBearerToken() {
    // Given
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer test-token");
    when(identityProviderManager.authenticate(any(TokenAuthenticationRequest.class)))
        .thenReturn(Uni.createFrom().item(securityIdentity));

    // When
    SecurityIdentity result =
        mechanism.authenticate(routingContext, identityProviderManager).await().indefinitely();

    // Then
    assertThat(result).isSameAs(securityIdentity);

    ArgumentCaptor<TokenAuthenticationRequest> requestCaptor =
        ArgumentCaptor.forClass(TokenAuthenticationRequest.class);
    verify(identityProviderManager).authenticate(requestCaptor.capture());

    TokenAuthenticationRequest authRequest = requestCaptor.getValue();
    assertThat(authRequest.getToken().getToken()).isEqualTo("test-token");
  }

  @Test
  void getChallenge_shouldReturnProperChallengeData() {
    // When
    ChallengeData challenge = mechanism.getChallenge(routingContext).await().indefinitely();

    // Then
    assertThat(challenge).isNotNull();
    assertThat(challenge.status).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
    assertThat(challenge.headerName).isEqualTo(HttpHeaders.WWW_AUTHENTICATE);
    assertThat(challenge.headerContent).isEqualTo("Bearer realm=\"beachape-api\"");
  }

  @Test
  void getCredentialTypes_shouldReturnTokenAuthenticationRequest() {
    // When/Then
    assertThat(mechanism.getCredentialTypes())
        .hasSize(1)
        .contains(TokenAuthenticationRequest.class);
  }

  @Test
  void authenticate_shouldPropagateException_whenIdentityProviderFails() {
    // Given
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer test-token");
    RuntimeException testException = new RuntimeException("Test error");
    when(identityProviderManager.authenticate(any()))
        .thenReturn(Uni.createFrom().failure(testException));

    // When/Then
    assertThatThrownBy(
            () ->
                mechanism
                    .authenticate(routingContext, identityProviderManager)
                    .await()
                    .indefinitely())
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Test error");
  }
}
