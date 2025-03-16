package com.beachape.quiam.domain.apikeys;

import jakarta.annotation.Nullable;

public interface ApiKeyService {

  String createApiKey(String userId);

  /**
   * Deletes the API key if it exists.
   *
   * @param apiKey the API key to be deleted
   * @return true if the API key existed and was deleted, otherwise false
   */
  boolean deleteApiKey(String apiKey);

  /**
   * Validates the provided API key.
   *
   * @param apiKey the API key to validate
   * @return the user ID if the key is validated, otherwise returns null
   */
  @Nullable String validateApiKey(String apiKey);
}
