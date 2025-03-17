package com.beachape.quiam.app.routes.users;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"JUnitClassModifiers", "NullAway"})
@QuarkusTest
class UsersResourceTest {

  @Test
  void upsertUser_shouldReturnSuccessMessage_whenSuccessful() {
    // Given
    String username = "testUser1";
    ApiModels.UpsertUserRequest request = new ApiModels.UpsertUserRequest(username, "password123");

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
    assertThat(response).isEqualTo(new ApiModels.UpsertUserResponse("User upserted successfully"));
  }

  @Test
  void authenticate_shouldReturnTokenAndSetCookie_whenCredentialsValid() {
    // Given
    String username = "testUser2";
    ApiModels.UpsertUserRequest upsertRequest =
        new ApiModels.UpsertUserRequest(username, "password123");
    given()
        .contentType("application/json")
        .body(upsertRequest)
        .when()
        .post("/api/users/_upsert")
        .then()
        .statusCode(200);

    ApiModels.AuthenticationRequest authRequest =
        new ApiModels.AuthenticationRequest(username, "password123");

    // When
    var response =
        given()
            .contentType("application/json")
            .body(authRequest)
            .when()
            .post("/api/users/_login")
            .then()
            .statusCode(200);

    // Then
    response.cookie("session", is(not(nullValue())));
    ApiModels.AuthenticationResponse authResponse =
        response.extract().as(ApiModels.AuthenticationResponse.class);
    assertThat(authResponse.username()).isEqualTo(username);
    assertThat(authResponse.token()).isNotNull();
  }

  @Test
  void authenticate_shouldReturn404_whenUserNotFound() {
    // Given
    ApiModels.AuthenticationRequest request =
        new ApiModels.AuthenticationRequest("nonexistentUser", "password123");

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

    assertThat(response.error()).isEqualTo("User not found");
  }

  @Test
  void authenticate_shouldReturn401_whenPasswordInvalid() {
    // Given
    String username = "testUser3";
    ApiModels.UpsertUserRequest upsertRequest =
        new ApiModels.UpsertUserRequest(username, "password123");
    given()
        .contentType("application/json")
        .body(upsertRequest)
        .when()
        .post("/api/users/_upsert")
        .then()
        .statusCode(200);

    ApiModels.AuthenticationRequest authRequest =
        new ApiModels.AuthenticationRequest(username, "wrongPassword");

    // When/Then
    ApiModels.ErrorResponse response =
        given()
            .contentType("application/json")
            .body(authRequest)
            .when()
            .post("/api/users/_login")
            .then()
            .statusCode(401)
            .extract()
            .as(ApiModels.ErrorResponse.class);

    assertThat(response.error()).isEqualTo("Invalid password");
  }

  @Test
  void logout_shouldClearCookie_whenValidToken() {
    // Given
    String username = "testUser4";
    ApiModels.UpsertUserRequest upsertRequest =
        new ApiModels.UpsertUserRequest(username, "password123");
    given()
        .contentType("application/json")
        .body(upsertRequest)
        .when()
        .post("/api/users/_upsert")
        .then()
        .statusCode(200);

    ApiModels.AuthenticationRequest authRequest =
        new ApiModels.AuthenticationRequest(username, "password123");
    var loginResponse =
        given()
            .contentType("application/json")
            .body(authRequest)
            .when()
            .post("/api/users/_login")
            .then()
            .statusCode(200)
            .extract()
            .as(ApiModels.AuthenticationResponse.class);

    // When
    var response =
        given()
            .cookie("session", loginResponse.token())
            .when()
            .post("/api/users/_logout")
            .then()
            .statusCode(200);

    // Then
    response.cookie("session", "");
  }

  @Test
  void logout_shouldReturn401_whenInvalidToken() {
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

    assertThat(response.error()).isEqualTo("Invalid token");
  }

  @Test
  void getUser_shouldReturn200_whenValidToken() {
    // Given
    String username = "testUser5";
    ApiModels.UpsertUserRequest upsertRequest =
        new ApiModels.UpsertUserRequest(username, "password123");
    given()
        .contentType("application/json")
        .body(upsertRequest)
        .when()
        .post("/api/users/_upsert")
        .then()
        .statusCode(200);

    ApiModels.AuthenticationRequest authRequest =
        new ApiModels.AuthenticationRequest(username, "password123");
    var loginResponse =
        given()
            .contentType("application/json")
            .body(authRequest)
            .when()
            .post("/api/users/_login")
            .then()
            .statusCode(200)
            .extract()
            .as(ApiModels.AuthenticationResponse.class);

    // When/Then
    given()
        .cookie("session", loginResponse.token())
        .when()
        .get("/api/users/me")
        .then()
        .statusCode(200);
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

    assertThat(response.error()).isEqualTo("No authentication mechanism found");
  }

  @Test
  void getUser_shouldReturn401_whenInvalidToken() {
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

    assertThat(response.error()).isEqualTo("Invalid token");
  }

  @Test
  void getUser_shouldAcceptBearerToken() {
    // Given
    String username = "testUser6";
    ApiModels.UpsertUserRequest upsertRequest =
        new ApiModels.UpsertUserRequest(username, "password123");
    given()
        .contentType("application/json")
        .body(upsertRequest)
        .when()
        .post("/api/users/_upsert")
        .then()
        .statusCode(200);

    ApiModels.AuthenticationRequest authRequest =
        new ApiModels.AuthenticationRequest(username, "password123");
    var loginResponse =
        given()
            .contentType(MediaType.APPLICATION_JSON)
            .body(authRequest)
            .when()
            .post("/api/users/_login")
            .then()
            .statusCode(200)
            .extract()
            .as(ApiModels.AuthenticationResponse.class);

    // Then use the token in Authorization header
    given()
        .header("Authorization", "Bearer " + loginResponse.token())
        .when()
        .get("/api/users/me")
        .then()
        .statusCode(200);
  }

  @Test
  void getUser_shouldReturn401_whenInvalidBearerToken() {
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

    assertThat(response.error()).isEqualTo("Invalid token");
  }

  @Test
  void logout_shouldAcceptBearerToken() {
    // Given
    String username = "testUser7";
    ApiModels.UpsertUserRequest upsertRequest =
        new ApiModels.UpsertUserRequest(username, "password123");
    given()
        .contentType("application/json")
        .body(upsertRequest)
        .when()
        .post("/api/users/_upsert")
        .then()
        .statusCode(200);

    ApiModels.AuthenticationRequest authRequest =
        new ApiModels.AuthenticationRequest(username, "password123");
    var loginResponse =
        given()
            .contentType(MediaType.APPLICATION_JSON)
            .body(authRequest)
            .when()
            .post("/api/users/_login")
            .then()
            .statusCode(200)
            .extract()
            .as(ApiModels.AuthenticationResponse.class);

    // Then use the token in Authorization header for logout
    given()
        .header("Authorization", "Bearer " + loginResponse.token())
        .when()
        .post("/api/users/_logout")
        .then()
        .statusCode(200);
  }

  @Test
  void logout_shouldReturn401_whenInvalidBearerToken() {
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

    assertThat(response.error()).isEqualTo("Invalid token");
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
