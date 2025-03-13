package com.beachape.quiam.infra.crypto;

import com.beachape.quiam.domain.crypto.AsymmetricKeysManager;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Implementation of AsymmetricKeysManager that generates new RSA keys on application startup.
 *
 * <p>This implementation generates a new key pair every time the application starts, which means
 * that any previously issued signatures or tokens will become invalid after application restart.
 *
 * <p>This implementation is suitable for development and testing environments, but for production
 * environments, consider using an implementation that maintains consistent keys across application
 * restarts.
 */
@Default
@ApplicationScoped
public class RegenOnAppStartAsymmetricKeysManager implements AsymmetricKeysManager {
  private PrivateKey privateKey;
  private PublicKey publicKey;

  /** Generates a new RSA key pair when the application starts. */
  public RegenOnAppStartAsymmetricKeysManager() {
    try {
      KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
      keyGen.initialize(2048);
      KeyPair pair = keyGen.generateKeyPair();
      this.privateKey = pair.getPrivate();
      this.publicKey = pair.getPublic();
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("Failed to generate key pair", e);
    }
  }

  @Override
  public PrivateKey getPrivateKey() {
    return privateKey;
  }

  @Override
  public PublicKey getPublicKey() {
    return publicKey;
  }
}
