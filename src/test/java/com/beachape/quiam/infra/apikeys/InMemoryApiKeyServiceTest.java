package com.beachape.quiam.infra.apikeys;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.beachape.quiam.domain.apikeys.ApiKeyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InMemoryApiKeyServiceTest {
  private InMemoryApiKeyService service;
  private static final String USER_ID = "test-user-123";

  @BeforeEach
  void setUp() {
    service = new InMemoryApiKeyService();
  }

  @Test
  void createApiKey_shouldCreateUniqueKeys() throws Exception {
    String key1 = service.createApiKey(USER_ID);
    String key2 = service.createApiKey(USER_ID);

    assertNotNull(key1);
    assertNotNull(key2);
    assertNotEquals(key1, key2);
  }

  @Test
  void createApiKey_shouldCreateValidatableKey() throws Exception {
    String apiKey = service.createApiKey(USER_ID);
    String userId = service.validateApiKey(apiKey);

    assertEquals(USER_ID, userId);
  }

  @Test
  void validateApiKey_shouldThrowException_whenKeyDoesNotExist() {
    assertThrows(
        ApiKeyService.ApiKeyNotFoundException.class,
        () -> service.validateApiKey("nonexistent-key"));
  }

  @Test
  void deleteApiKey_shouldMakeKeyInvalid() throws Exception {
    // Given
    String apiKey = service.createApiKey(USER_ID);

    // When
    service.deleteApiKey(apiKey);

    // Then
    assertThrows(ApiKeyService.ApiKeyNotFoundException.class, () -> service.validateApiKey(apiKey));
  }

  @Test
  void deleteApiKey_shouldThrowException_whenKeyDoesNotExist() {
    assertThrows(
        ApiKeyService.ApiKeyNotFoundException.class, () -> service.deleteApiKey("nonexistent-key"));
  }

  @Test
  void multipleUsers_shouldNotInterfereWithEachOther() throws Exception {
    // Given
    String user1 = "user-1";
    String user2 = "user-2";

    // When
    String key1 = service.createApiKey(user1);
    String key2 = service.createApiKey(user2);

    // Then
    assertEquals(user1, service.validateApiKey(key1));
    assertEquals(user2, service.validateApiKey(key2));

    // When deleting one key
    service.deleteApiKey(key1);

    // Then the other should still be valid
    assertThrows(ApiKeyService.ApiKeyNotFoundException.class, () -> service.validateApiKey(key1));
    assertEquals(user2, service.validateApiKey(key2));
  }
}
