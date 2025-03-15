package com.beachape.quiam.infra.users;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.beachape.quiam.domain.users.UsersService.InvalidPassword;
import com.beachape.quiam.domain.users.UsersService.NoSuchUser;
import com.beachape.quiam.domain.users.UsersService.UpsertUser;
import com.beachape.quiam.domain.users.UsersService.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class InMemoryUsersServiceTest {

  private InMemoryUsersService service;
  private static final String USERNAME = "testUser";
  private static final String PASSWORD = "testPassword123!";

  @BeforeEach
  void setUp() {
    service = new InMemoryUsersService();
  }

  @Test
  void upsert_shouldStoreNewUser() throws Exception {
    // Given
    UpsertUser newUser = new UpsertUser(USERNAME, PASSWORD);

    // When
    service.upsert(newUser);

    // Then
    User authenticatedUser = service.authenticate(USERNAME, PASSWORD);
    assertThat(authenticatedUser.name()).isEqualTo(USERNAME);
  }

  @Test
  void upsert_shouldUpdateExistingUser() throws Exception {
    // Given
    String oldPassword = "oldPassword123!";
    String newPassword = "newPassword123!";

    service.upsert(new UpsertUser(USERNAME, oldPassword));

    // When
    service.upsert(new UpsertUser(USERNAME, newPassword));

    // Then
    assertThatThrownBy(() -> service.authenticate(USERNAME, oldPassword))
        .isInstanceOf(InvalidPassword.class);
    User authenticatedUser = service.authenticate(USERNAME, newPassword);
    assertThat(authenticatedUser.name()).isEqualTo(USERNAME);
  }

  @Test
  void authenticate_shouldThrowNoSuchUser_whenUserDoesNotExist() throws Exception {
    assertThatThrownBy(() -> service.authenticate("nonexistentUser", PASSWORD))
        .isInstanceOf(NoSuchUser.class);
  }

  @Test
  void authenticate_shouldThrowInvalidPassword_whenPasswordIsIncorrect() throws Exception {
    // Given
    service.upsert(new UpsertUser(USERNAME, PASSWORD));

    // When/Then
    assertThatThrownBy(() -> service.authenticate(USERNAME, "wrongPassword"))
        .isInstanceOf(InvalidPassword.class);
  }

  @Test
  void authenticate_shouldReturnUser_whenCredentialsAreValid() throws Exception {
    // Given
    service.upsert(new UpsertUser(USERNAME, PASSWORD));

    // When
    User authenticatedUser = service.authenticate(USERNAME, PASSWORD);

    // Then
    assertThat(authenticatedUser.name()).isEqualTo(USERNAME);
    assertThat(authenticatedUser.passwordHash()).isNotNull();
  }

  @Test
  void authenticate_shouldSucceed_withMultipleUsers() throws Exception {
    // Given
    String user1 = "user1";
    String pass1 = "pass1";
    String user2 = "user2";
    String pass2 = "pass2";

    service.upsert(new UpsertUser(user1, pass1));
    service.upsert(new UpsertUser(user2, pass2));

    // When/Then
    User auth1 = service.authenticate(user1, pass1);
    User auth2 = service.authenticate(user2, pass2);

    assertThat(auth1.name()).isEqualTo(user1);
    assertThat(auth2.name()).isEqualTo(user2);
  }

  @Test
  void authenticate_shouldThrowInvalidPassword_forWrongPasswordWithValidUsername()
      throws Exception {
    // Given
    service.upsert(new UpsertUser(USERNAME, PASSWORD));

    // When/Then
    assertThatThrownBy(() -> service.authenticate(USERNAME, PASSWORD + "wrong"))
        .isInstanceOf(InvalidPassword.class);
  }
}
