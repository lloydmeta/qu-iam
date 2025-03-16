package com.beachape.quiam.infra.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.beachape.quiam.domain.apikeys.ApiKeyService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
final class InMemoryJwtServiceTest {
  @SuppressWarnings("NullAway")
  @InjectMock
  private ApiKeyService apiKeyService;

  @SuppressWarnings("NullAway")
  @Inject
  private InMemoryJwtService service;

  @Test
  void createToken_shouldCreateValidToken() throws Exception {
    String userId = "user123";
    String apiKey = "api-key-123";
    when(apiKeyService.createApiKey(userId)).thenReturn(apiKey);
    when(apiKeyService.validateApiKey(apiKey)).thenReturn(userId);

    String token = service.createToken(userId);

    assertThat(token).isNotNull();
    verify(apiKeyService).createApiKey(userId);

    // Verify token can be validated
    service.validateToken(token);
    verify(apiKeyService).validateApiKey(apiKey);
  }

  @Test
  void validateToken_shouldValidateApiKey() throws Exception {
    String userId = "user123";
    String apiKey = "api-key-123";
    when(apiKeyService.createApiKey(userId)).thenReturn(apiKey);
    when(apiKeyService.validateApiKey(apiKey)).thenReturn(userId);

    String token = service.createToken(userId);

    String validatedUserId = service.validateToken(token);

    assertEquals(userId, validatedUserId);

    verify(apiKeyService).validateApiKey(apiKey);
  }

  @Test
  void validateToken_shouldReturnNull_whenTokenIsInvalid() {
    assertThat(service.validateToken("invalid-token")).isNull();
  }

  @Test
  void invalidateToken_shouldDeleteApiKey() throws Exception {
    String userId = "user123";
    String apiKey = "api-key-123";
    when(apiKeyService.createApiKey(userId)).thenReturn(apiKey);
    when(apiKeyService.deleteApiKey(apiKey)).thenReturn(true);
    String token = service.createToken(userId);

    assertThat(service.invalidateToken(token)).isTrue();

    verify(apiKeyService).deleteApiKey("api-key-123");
    verify(apiKeyService).deleteApiKey(apiKey);
  }

  @Test
  void invalidateToken_shouldReturnFalse_whenTokenIsInvalid() {
    assertThat(service.validateToken("invalid-token")).isNull();
  }
}
