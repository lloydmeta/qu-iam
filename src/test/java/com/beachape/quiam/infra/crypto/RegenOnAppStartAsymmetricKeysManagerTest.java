package com.beachape.quiam.infra.crypto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

final class RegenOnAppStartAsymmetricKeysManagerTest {

  @Test
  void onStart_shouldGenerateValidKeyPair() {
    RegenOnAppStartAsymmetricKeysManager manager = new RegenOnAppStartAsymmetricKeysManager();

    assertThat(manager.getPrivateKey()).isNotNull();
    assertThat(manager.getPublicKey()).isNotNull();
    assertThat(manager.getPrivateKey().getAlgorithm()).isEqualTo("RSA");
    assertThat(manager.getPublicKey().getAlgorithm()).isEqualTo("RSA");
  }

  @Test
  void differentInstances_shouldHaveDifferentKeys() {
    RegenOnAppStartAsymmetricKeysManager manager1 = new RegenOnAppStartAsymmetricKeysManager();
    RegenOnAppStartAsymmetricKeysManager manager2 = new RegenOnAppStartAsymmetricKeysManager();

    assertThat(manager1.getPrivateKey().getEncoded())
        .isNotEqualTo(manager2.getPrivateKey().getEncoded());
    assertThat(manager1.getPublicKey().getEncoded())
        .isNotEqualTo(manager2.getPublicKey().getEncoded());
  }
}
