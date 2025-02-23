package com.beachape.quiam.infra.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.beachape.quiam.domain.apikeys.ApiKeyService;
import com.beachape.quiam.domain.jwt.JwtService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
class InMemoryJwtServiceTest {
  @SuppressWarnings("NullAway")
  @InjectMock
  private ApiKeyService apiKeyService;

  @SuppressWarnings("NullAway")
  @Inject
  private InMemoryJwtService service;

  @Test
  void createToken_shouldCreateValidToken() throws Exception {
    String userId = "user123";
    when(apiKeyService.createApiKey(userId)).thenReturn("api-key-123");

    String token = service.createToken(userId);

    assertNotNull(token);
    verify(apiKeyService).createApiKey(userId);

    // Verify token can be validated
    service.validateToken(token);
  }

  @Test
  void validateToken_shouldValidateApiKey() throws Exception {
    String userId = "user123";
    when(apiKeyService.createApiKey(userId)).thenReturn("api-key-123");
    String token = service.createToken(userId);

    String validatedUserId = service.validateToken(token);

    assertEquals(userId, validatedUserId);

    verify(apiKeyService).validateApiKey("api-key-123");
  }

  @Test
  void validateToken_shouldThrowException_whenTokenIsInvalid() {
    assertThrows(
        JwtService.TokenValidationException.class, () -> service.validateToken("invalid-token"));
  }

  @Test
  void invalidateToken_shouldDeleteApiKey() throws Exception {
    String userId = "user123";
    when(apiKeyService.createApiKey(userId)).thenReturn("api-key-123");
    String token = service.createToken(userId);

    service.invalidateToken(token);

    verify(apiKeyService).deleteApiKey("api-key-123");
  }

  @Test
  void invalidateToken_shouldThrowException_whenTokenIsInvalid() {
    assertThrows(
        JwtService.TokenValidationException.class, () -> service.invalidateToken("invalid-token"));
  }
}
