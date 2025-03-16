package com.beachape.quiam.domain.apikeys;

import com.beachape.quiam.domain.errors.DomainException;

public interface ApiKeyService {

  String createApiKey(String userId);

  void deleteApiKey(String apiKey) throws ApiKeyNotFoundException;

  String validateApiKey(String apiKey) throws ApiKeyNotFoundException;

  public class ApiKeyNotFoundException extends DomainException {}
}
