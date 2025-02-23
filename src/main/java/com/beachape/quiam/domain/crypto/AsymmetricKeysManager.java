package com.beachape.quiam.domain.crypto;

import java.security.PrivateKey;
import java.security.PublicKey;

/** Interface for managing asymmetric key pairs used for cryptographic operations. */
public interface AsymmetricKeysManager {
  /**
   * Returns the private key of the asymmetric key pair.
   *
   * @return The private key
   */
  PrivateKey getPrivateKey();

  /**
   * Returns the public key of the asymmetric key pair.
   *
   * @return The public key
   */
  PublicKey getPublicKey();
}
