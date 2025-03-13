package com.beachape.quiam.infra.users;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;

final class PasswordHasher {
  private static final int SALT_LENGTH = 32;
  private static final int HASH_LENGTH = 256; // bits
  private static final int ITERATIONS = 600000;
  private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
  private final SecureRandom SECURE_RANDOM = new SecureRandom();

  static {
    Security.addProvider(new BouncyCastleFipsProvider());
  }

  String hashPassword(String password) {
    byte[] salt = new byte[SALT_LENGTH];
    SECURE_RANDOM.nextBytes(salt);

    PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, HASH_LENGTH);

    SecretKeyFactory skf;
    try {
      skf = SecretKeyFactory.getInstance(ALGORITHM, "BCFIPS");
    } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
      throw new IllegalArgumentException(e);
    }
    byte[] hash;
    try {
      hash = skf.generateSecret(spec).getEncoded();
    } catch (InvalidKeySpecException e) {
      throw new IllegalArgumentException(e);
    }
    spec.clearPassword();

    String saltString = Base64.getEncoder().encodeToString(salt);
    String hashString = Base64.getEncoder().encodeToString(hash);

    return ITERATIONS + ":" + saltString + ":" + hashString;
  }

  @SuppressWarnings("StringSplitter")
  boolean verifyPassword(String password, String hashedPassword) {
    try {
      String[] parts = hashedPassword.split(":");
      if (parts.length != 3) {
        return false;
      }

      int iterations = Integer.parseInt(parts[0]);
      byte[] salt = Base64.getDecoder().decode(parts[1]);
      byte[] expectedHash = Base64.getDecoder().decode(parts[2]);

      PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, HASH_LENGTH);

      SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM, "BCFIPS");
      byte[] actualHash = skf.generateSecret(spec).getEncoded();
      spec.clearPassword();

      return constantTimeEquals(expectedHash, actualHash);
    } catch (NumberFormatException
        | NoSuchAlgorithmException
        | NoSuchProviderException
        | InvalidKeySpecException e) {
      return false;
    } catch (Exception e) {
      return false;
    }
  }

  private boolean constantTimeEquals(byte[] a, byte[] b) {
    if (a.length != b.length) {
      return false;
    }

    int result = 0;
    for (int i = 0; i < a.length; i++) {
      result |= a[i] ^ b[i];
    }
    return result == 0;
  }
}
