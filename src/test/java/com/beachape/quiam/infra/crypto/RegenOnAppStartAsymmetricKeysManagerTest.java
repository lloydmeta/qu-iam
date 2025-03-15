package com.beachape.quiam.infra.crypto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

final class RegenOnAppStartAsymmetricKeysManagerTest {

  @Test
  void onStart_shouldGenerateValidKeyPair() {
    RegenOnAppStartAsymmetricKeysManager manager = new RegenOnAppStartAsymmetricKeysManager();

    assertThat(manager.getPrivateKey()).isNotNull();
    assertThat(manager.getPublicKey()).isNotNull();
    assertEquals("RSA", manager.getPrivateKey().getAlgorithm());
    assertEquals("RSA", manager.getPublicKey().getAlgorithm());
  }

  @Test
  void differentInstances_shouldHaveDifferentKeys() {
    RegenOnAppStartAsymmetricKeysManager manager1 = new RegenOnAppStartAsymmetricKeysManager();
    RegenOnAppStartAsymmetricKeysManager manager2 = new RegenOnAppStartAsymmetricKeysManager();

    assertNotEquals(manager1.getPrivateKey().getEncoded(), manager2.getPrivateKey().getEncoded());
    assertNotEquals(manager1.getPublicKey().getEncoded(), manager2.getPublicKey().getEncoded());
  }
}
