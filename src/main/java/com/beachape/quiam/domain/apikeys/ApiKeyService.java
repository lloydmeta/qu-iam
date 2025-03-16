package com.beachape.quiam.domain.apikeys;

import jakarta.annotation.Nullable;

/** Service interface for managing API keys. */
public interface ApiKeyService {

  /**
   * Generates a new API key for the specified user.
   *
   * @param userId the unique identifier of the user for whom the API key is being created
   * @return the newly created API key as a String
   */
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
