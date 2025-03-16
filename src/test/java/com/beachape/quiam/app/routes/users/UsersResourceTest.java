package com.beachape.quiam.app.routes.users;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.beachape.quiam.domain.jwt.JwtService;
import com.beachape.quiam.domain.users.UsersService;
import com.beachape.quiam.domain.users.UsersService.UpsertUser;
import com.beachape.quiam.domain.users.UsersService.User;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import static io.restassured.RestAssured.given;
import jakarta.ws.rs.core.MediaType;

@SuppressWarnings({"JUnitClassModifiers", "NullAway"})
@QuarkusTest
class UsersResourceTest {

  @SuppressWarnings("NullAway")
  @InjectMock
  UsersService usersService;

  @SuppressWarnings("NullAway")
  @InjectMock
  JwtService jwtService;

  @Test
  void upsertUser_shouldReturnSuccessMessage_whenSuccessful() {
    // Given
    ApiModels.UpsertUserRequest request =
        new ApiModels.UpsertUserRequest("testUser", "password123");
    UpsertUser domainUser = new UpsertUser("testUser", "password123");

    // When
    ApiModels.UpsertUserResponse response =
        given()
            .contentType("application/json")
            .body(request)
            .when()
            .post("/api/users/_upsert")
            .then()
            .statusCode(200)
            .extract()
            .as(ApiModels.UpsertUserResponse.class);

    // Then
    assertEquals(new ApiModels.UpsertUserResponse("User upserted successfully"), response);
    verify(usersService).upsert(domainUser);
  }

  @Test
  void authenticate_shouldReturnTokenAndSetCookie_whenCredentialsValid() throws Exception {
    // Given
    ApiModels.AuthenticationRequest request =
        new ApiModels.AuthenticationRequest("testUser", "password123");
    when(usersService.authenticate("testUser", "password123"))
        .thenReturn(new User("testUser", "hashedPassword"));
    when(jwtService.createToken("testUser")).thenReturn("jwt-token-123");

    // When
    var response =
        given()
            .contentType("application/json")
            .body(request)
            .when()
            .post("/api/users/_login")
            .then()
            .statusCode(200);

    // Then
    response.cookie("session", "jwt-token-123");
    ApiModels.AuthenticationResponse authResponse =
        response.extract().as(ApiModels.AuthenticationResponse.class);
    assertEquals("testUser", authResponse.username());
    assertEquals("jwt-token-123", authResponse.token());
  }

  @Test
  void authenticate_shouldReturn404_whenUserNotFound() throws Exception {
    // Given
    ApiModels.AuthenticationRequest request =
        new ApiModels.AuthenticationRequest("testUser", "password123");
    when(usersService.authenticate("testUser", "password123"))
        .thenThrow(new UsersService.NoSuchUser());

    // When/Then
    ApiModels.ErrorResponse response =
        given()
            .contentType("application/json")
            .body(request)
            .when()
            .post("/api/users/_login")
            .then()
            .statusCode(404)
            .extract()
            .as(ApiModels.ErrorResponse.class);

    assertEquals("User not found", response.error());
  }

  @Test
  void authenticate_shouldReturn401_whenPasswordInvalid() throws Exception {
    // Given
    ApiModels.AuthenticationRequest request =
        new ApiModels.AuthenticationRequest("testUser", "password123");
    when(usersService.authenticate("testUser", "password123"))
        .thenThrow(new UsersService.InvalidPassword());

    // When/Then
    ApiModels.ErrorResponse response =
        given()
            .contentType("application/json")
            .body(request)
            .when()
            .post("/api/users/_login")
            .then()
            .statusCode(401)
            .extract()
            .as(ApiModels.ErrorResponse.class);

    assertEquals("Invalid password", response.error());
  }

  @Test
  void logout_shouldClearCookie_whenValidToken() throws Exception {
    // Given
    when(jwtService.validateToken("valid-token")).thenReturn("test-user");
    when(jwtService.invalidateToken("valid-token")).thenReturn(true);
    // When
    var response =
        given()
            .cookie("session", "valid-token")
            .when()
            .post("/api/users/_logout")
            .then()
            .statusCode(200);

    // Then
    response.cookie("session", "");
    verify(jwtService).validateToken("valid-token");
    verify(jwtService).invalidateToken("valid-token");
  }

  @Test
  void logout_shouldReturn400_whenInvalidToken() throws Exception {
    // Given
    when(jwtService.validateToken("invalid-token")).thenReturn(null);
    when(jwtService.invalidateToken("invalid-token")).thenReturn(false);

    // When/Then
    ApiModels.ErrorResponse response =
        given()
            .cookie("session", "invalid-token")
            .when()
            .post("/api/users/_logout")
            .then()
            .statusCode(401)
            .extract()
            .as(ApiModels.ErrorResponse.class);

    verify(jwtService).validateToken("invalid-token");

    assertEquals("Invalid token", response.error());
  }

  @Test
  void getUser_shouldReturn200_whenValidToken() throws Exception {
    // Given
    when(jwtService.validateToken("valid-token")).thenReturn("test-user");
    // When/Then
    given().cookie("session", "valid-token").when().get("/api/users/me").then().statusCode(200);

    verify(jwtService).validateToken("valid-token");
  }

  @Test
  void getUser_shouldReturn401_whenNoToken() {
    // When/Then
    ApiModels.ErrorResponse response =
        given()
            .when()
            .get("/api/users/me")
            .then()
            .statusCode(401)
            .contentType("application/json")
            .extract()
            .as(ApiModels.ErrorResponse.class);

    assertEquals("No authentication mechanism found", response.error());
  }

  @Test
  void getUser_shouldReturn401_whenInvalidToken() throws Exception {
    // Given
    when(jwtService.validateToken("invalid-token")).thenReturn(null);

    // When/Then
    ApiModels.ErrorResponse response =
        given()
            .cookie("session", "invalid-token")
            .when()
            .get("/api/users/me")
            .then()
            .statusCode(401)
            .extract()
            .as(ApiModels.ErrorResponse.class);

    assertEquals("Invalid token", response.error());
  }

  @Test
  void getUser_shouldAcceptBearerToken() throws Exception {
    // Given
    when(usersService.authenticate("test-user", "test-pass"))
        .thenReturn(new User("test-user", "test-pass"));
    when(jwtService.createToken("test-user")).thenReturn("valid-token");
    when(jwtService.validateToken("valid-token")).thenReturn("test-user");

    // First authenticate to get the token
    ApiModels.AuthenticationResponse authResponse =
        given()
            .contentType(MediaType.APPLICATION_JSON)
            .body(new ApiModels.AuthenticationRequest("test-user", "test-pass"))
            .when()
            .post("/api/users/_login")
            .then()
            .statusCode(200)
            .extract()
            .as(ApiModels.AuthenticationResponse.class);

    // Then use the token in Authorization header
    given()
        .header("Authorization", "Bearer " + authResponse.token())
        .when()
        .get("/api/users/me")
        .then()
        .statusCode(200);
  }

  @Test
  void getUser_shouldReturn401_whenInvalidBearerToken() throws Exception {
    // Given
    when(jwtService.validateToken("invalid-token")).thenReturn(null);

    // When/Then
    ApiModels.ErrorResponse response =
        given()
            .header("Authorization", "Bearer invalid-token")
            .when()
            .get("/api/users/me")
            .then()
            .statusCode(401)
            .contentType(MediaType.APPLICATION_JSON)
            .extract()
            .as(ApiModels.ErrorResponse.class);

    assertEquals("Invalid token", response.error());
  }

  @Test
  void logout_shouldAcceptBearerToken() throws Exception {
    // Given
    when(usersService.authenticate("test-user", "test-pass"))
        .thenReturn(new User("test-user", "test-pass"));
    when(jwtService.createToken("test-user")).thenReturn("valid-token");
    when(jwtService.validateToken("valid-token")).thenReturn("test-user");
    when(jwtService.invalidateToken("valid-token")).thenReturn(true);

    // First authenticate to get the token
    ApiModels.AuthenticationResponse authResponse =
        given()
            .contentType(MediaType.APPLICATION_JSON)
            .body(new ApiModels.AuthenticationRequest("test-user", "test-pass"))
            .when()
            .post("/api/users/_login")
            .then()
            .statusCode(200)
            .extract()
            .as(ApiModels.AuthenticationResponse.class);

    // Then use the token in Authorization header for logout
    given()
        .header("Authorization", "Bearer " + authResponse.token())
        .when()
        .post("/api/users/_logout")
        .then()
        .statusCode(200);

    verify(jwtService).validateToken(authResponse.token());
    verify(jwtService).invalidateToken(authResponse.token());
  }

  @Test
  void logout_shouldReturn401_whenInvalidBearerToken() throws Exception {
    // Given
    when(jwtService.validateToken("invalid-token")).thenReturn(null);

    // When/Then
    ApiModels.ErrorResponse response =
        given()
            .header("Authorization", "Bearer invalid-token")
            .when()
            .post("/api/users/_logout")
            .then()
            .statusCode(401)
            .contentType(MediaType.APPLICATION_JSON)
            .extract()
            .as(ApiModels.ErrorResponse.class);

    assertEquals("Invalid token", response.error());
  }

  @Test
  void upsertUser_shouldReturn400_whenRequestBodyIsMissing() {
    given()
        .contentType("application/json")
        .when()
        .post("/api/users/_upsert")
        .then()
        .statusCode(400)
        .contentType("application/json");
  }

  @Test
  void upsertUser_shouldReturn400_whenRequestBodyIsEmpty() {
    given()
        .contentType("application/json")
        .body("{}")
        .when()
        .post("/api/users/_upsert")
        .then()
        .statusCode(400)
        .contentType("application/json");
  }

  @Test
  void login_shouldReturn400_whenRequestBodyIsMissing() {
    given()
        .contentType("application/json")
        .when()
        .post("/api/users/_login")
        .then()
        .statusCode(400)
        .contentType("application/json");
  }

  @Test
  void login_shouldReturn400_whenRequestBodyIsEmpty() {
    given()
        .contentType("application/json")
        .body("{}")
        .when()
        .post("/api/users/_login")
        .then()
        .statusCode(400)
        .contentType("application/json");
  }

  @Test
  void login_shouldReturn400_whenUsernameIsMissing() {
    ApiModels.AuthenticationRequest request =
        new ApiModels.AuthenticationRequest(null, "password123");

    given()
        .contentType("application/json")
        .body(request)
        .when()
        .post("/api/users/_login")
        .then()
        .statusCode(400)
        .contentType("application/json");
  }

  @Test
  void login_shouldReturn400_whenPasswordIsMissing() {
    ApiModels.AuthenticationRequest request = new ApiModels.AuthenticationRequest("testUser", null);

    given()
        .contentType("application/json")
        .body(request)
        .when()
        .post("/api/users/_login")
        .then()
        .statusCode(400)
        .contentType("application/json");
  }
}
