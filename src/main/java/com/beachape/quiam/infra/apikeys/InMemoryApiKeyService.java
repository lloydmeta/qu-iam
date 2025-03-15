package com.beachape.quiam.infra.apikeys;

import static java.util.UUID.randomUUID;

import com.beachape.quiam.domain.apikeys.ApiKeyService;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Default
@ApplicationScoped
public class InMemoryApiKeyService implements ApiKeyService {
  private final Map<String, String> apiKeys = new ConcurrentHashMap<>(); // apiKey -> userId

  @Override
  @WithSpan
  public String createApiKey(String userId) {

    String apiKey = randomUUID().toString();
    apiKeys.put(apiKey, userId);
    return apiKey;
  }

  @Override
  @WithSpan
  public void deleteApiKey(String apiKey) throws ApiKeyNotFoundException {
    if (apiKeys.remove(apiKey) == null) {
      throw new ApiKeyNotFoundException();
    }
  }

  @Override
  @WithSpan
  public String validateApiKey(String apiKey) throws ApiKeyNotFoundException {
    String userId = apiKeys.get(apiKey);
    if (userId == null) {
      throw new ApiKeyNotFoundException();
    }
    return userId;
  }
}
