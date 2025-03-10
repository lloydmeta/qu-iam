package com.beachape.quiam.app.authentication;

import static org.junit.jupiter.api.Assertions.*;
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
class JwtBearerAuthenticationMechanismTest {
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
    assertNull(result.await().indefinitely());
    verify(identityProviderManager, never()).authenticate(any());
  }

  @Test
  void authenticate_shouldReturnNullUni_whenNotBearerToken() {
    // Given
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Basic abc123");

    // When
    Uni<SecurityIdentity> result = mechanism.authenticate(routingContext, identityProviderManager);

    // Then
    assertNull(result.await().indefinitely());
    verify(identityProviderManager, never()).authenticate(any());
  }

  @Test
  void authenticate_shouldReturnNullUni_whenEmptyToken() {
    // Given
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer ");

    // When
    Uni<SecurityIdentity> result = mechanism.authenticate(routingContext, identityProviderManager);

    // Then
    assertNull(result.await().indefinitely());
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
    assertSame(securityIdentity, result);

    ArgumentCaptor<TokenAuthenticationRequest> requestCaptor =
        ArgumentCaptor.forClass(TokenAuthenticationRequest.class);
    verify(identityProviderManager).authenticate(requestCaptor.capture());

    TokenAuthenticationRequest authRequest = requestCaptor.getValue();
    assertEquals("test-token", authRequest.getToken().getToken());
  }

  @Test
  void getChallenge_shouldReturnProperChallengeData() {
    // When
    ChallengeData challenge = mechanism.getChallenge(routingContext).await().indefinitely();

    // Then
    assertNotNull(challenge);
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), challenge.status);
    assertEquals(HttpHeaders.WWW_AUTHENTICATE, challenge.headerName);
    assertEquals("Bearer realm=\"beachape-api\"", challenge.headerContent);
  }

  @Test
  void getCredentialTypes_shouldReturnTokenAuthenticationRequest() {
    // When/Then
    assertEquals(1, mechanism.getCredentialTypes().size());
    assertTrue(mechanism.getCredentialTypes().contains(TokenAuthenticationRequest.class));
  }

  @Test
  void authenticate_shouldPropagateException_whenIdentityProviderFails() {
    // Given
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer test-token");
    RuntimeException testException = new RuntimeException("Test error");
    when(identityProviderManager.authenticate(any()))
        .thenReturn(Uni.createFrom().failure(testException));

    // When/Then
    assertThrows(
        RuntimeException.class,
        () ->
            mechanism.authenticate(routingContext, identityProviderManager).await().indefinitely());
  }
}
