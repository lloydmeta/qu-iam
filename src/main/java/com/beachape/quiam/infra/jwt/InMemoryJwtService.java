package com.beachape.quiam.infra.jwt;

import com.beachape.quiam.domain.apikeys.ApiKeyService;
import com.beachape.quiam.domain.crypto.AsymmetricKeysManager;
import com.beachape.quiam.domain.jwt.JwtService;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Default
@ApplicationScoped
public class InMemoryJwtService implements JwtService {
  private final ApiKeyService apiKeyService;
  private final AsymmetricKeysManager keysManager;
  private final JWTParser parser;

  public static final String API_KEY_CLAIM = "api_key";

  @Inject
  public InMemoryJwtService(
      ApiKeyService apiKeyService, AsymmetricKeysManager keysManager, JWTParser parser) {
    this.apiKeyService = apiKeyService;
    this.keysManager = keysManager;
    this.parser = parser;
  }

  @Override
  @WithSpan
  public String createToken(String userId) {

    String apiKey = apiKeyService.createApiKey(userId);
    return Jwt.claims()
        .issuer("beachape-api")
        .subject(userId)
        .claim(API_KEY_CLAIM, apiKey)
        .sign(keysManager.getPrivateKey());
  }

  @Override
  @WithSpan
  public String validateToken(String token) throws TokenValidationException {
    try {
      JsonWebToken jwt = parser.verify(token, keysManager.getPublicKey());
      String apiKey = jwt.getClaim(API_KEY_CLAIM).toString();
      if (apiKeyService.validateApiKey(apiKey) == null) {
        throw new TokenValidationException("Invalid API key.");
      } else {
        return jwt.getSubject();
      }
    } catch (ParseException e) {
      throw new TokenValidationException(e);
    }
  }

  @Override
  @WithSpan
  public void invalidateToken(String token) throws TokenValidationException {
    try {
      JsonWebToken jwt = parser.verify(token, keysManager.getPublicKey());
      String apiKey = jwt.getClaim(API_KEY_CLAIM).toString();
      if (!apiKeyService.deleteApiKey(apiKey)) {
        throw new TokenValidationException("Token does not exist in backend.");
      }
    } catch (ParseException e) {
      throw new TokenValidationException(e);
    }
  }
}
