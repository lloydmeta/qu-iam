package com.beachape.quiam.infra.apikeys;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class InMemoryApiKeyServiceTest {
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

    assertThat(key1).isNotNull();
    assertThat(key2).isNotNull();
    assertThat(key1).isNotEqualTo(key2);
  }

  @Test
  void createApiKey_shouldCreateValidatableKey() throws Exception {
    String apiKey = service.createApiKey(USER_ID);
    String userId = service.validateApiKey(apiKey);

    assertThat(userId).isEqualTo(USER_ID);
  }

  @Test
  void validateApiKey_shouldReturnNull_whenKeyDoesNotExist() {
    assertThat(service.validateApiKey("nonexistent-key")).isNull();
  }

  @Test
  void deleteApiKey_shouldMakeKeyInvalid() throws Exception {
    // Given
    String apiKey = service.createApiKey(USER_ID);

    // When
    service.deleteApiKey(apiKey);

    // Then
    assertThat(service.validateApiKey(apiKey)).isNull();
  }

  @Test
  void deleteApiKey_shouldReturnFalse_whenKeyDoesNotExist() {
    assertThat(service.deleteApiKey("nonexistent-key")).isFalse();
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
    assertThat(service.validateApiKey(key1)).isEqualTo(user1);
    assertThat(service.validateApiKey(key2)).isEqualTo(user2);

    // When deleting one key
    service.deleteApiKey(key1);
    assertThat(service.validateApiKey(key1)).isNull();

    // Then the other should still be valid
    assertThat(service.validateApiKey(key2)).isEqualTo(user2);
  }
}
