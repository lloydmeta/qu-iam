package com.beachape.quiam.infra.users;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

@SuppressWarnings("StringSplitter")
final class PasswordHasherTest {

  private PasswordHasher hasher;

  @BeforeEach
  void setUp() {
    hasher = new PasswordHasher();
  }

  @Test
  void hashPassword_shouldGenerateValidHash() {
    // Given
    String password = "correctPassword123!";

    // When
    String hashedPassword = hasher.hashPassword(password);

    // Then
    String[] parts = hashedPassword.split(":");
    assertEquals(3, parts.length);
    assertEquals("600000", parts[0]);
    assertThat(parts[1]).isNotEmpty(); // salt
    assertThat(parts[2]).isNotEmpty(); // hash
  }

  @Test
  void verifyPassword_shouldReturnTrue_whenPasswordIsCorrect() {
    // Given
    String password = "correctPassword123!";
    String hashedPassword = hasher.hashPassword(password);

    // When
    boolean result = hasher.verifyPassword(password, hashedPassword);

    // Then
    assertThat(result).isTrue();
  }

  @Test
  void verifyPassword_shouldReturnFalse_whenPasswordIsIncorrect() {
    // Given
    String password = "correctPassword123!";
    String wrongPassword = "wrongPassword123!";
    String hashedPassword = hasher.hashPassword(password);

    // When
    boolean result = hasher.verifyPassword(wrongPassword, hashedPassword);

    // Then
    assertThat(result).isFalse();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "", // empty string
        "1:!@#:hash", // invalid base64 salt
        "1:salt:!@#", // invalid base64 hash
        ":", // single colon
        "a:b", // two parts
        "a:b:c:d", // four parts
        "notanumber:salt:hash" // invalid iteration count
      })
  void verifyPassword_shouldReturnFalse_whenHashFormatIsInvalid(String invalidHash) {
    assertThat(hasher.verifyPassword("anyPassword", invalidHash)).isFalse();
  }

  @MethodSource("passwordTestCases")
  @ParameterizedTest
  void shouldVerify_variousPasswordTypes(String password) {
    // Given
    String hashedPassword = hasher.hashPassword(password);

    // When
    boolean result = hasher.verifyPassword(password, hashedPassword);

    // Then
    assertThat(result).isTrue();
  }

  private static Stream<Arguments> passwordTestCases() {
    return Stream.of(
        arguments("simple"),
        arguments("Complex Password 123!@#"),
        arguments("वेरी लॉन्ग पासवर्ड"), // Unicode password
        arguments(" leading and trailing spaces "),
        arguments("a".repeat(100)) // very long password
        );
  }

  @Test
  void hashPassword_shouldGenerateDifferentHashes_forSamePassword() {
    // Given
    String password = "samePassword123!";

    // When
    String hash1 = hasher.hashPassword(password);
    String hash2 = hasher.hashPassword(password);

    // Then
    assertNotEquals(hash1, hash2);
  }

  @Test
  void verifyPassword_shouldReturnFalse_whenIterationsAreDifferent() {
    // Given
    String password = "testPassword";
    String hashedPassword = hasher.hashPassword(password);
    String[] parts = hashedPassword.split(":");
    String modifiedHash = "500000:" + parts[1] + ":" + parts[2];

    // When
    boolean result = hasher.verifyPassword(password, modifiedHash);

    // Then
    assertThat(result).isFalse();
  }
}
