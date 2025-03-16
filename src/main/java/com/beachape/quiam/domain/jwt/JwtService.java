package com.beachape.quiam.domain.jwt;

import jakarta.annotation.Nullable;

/**
 * Service for managing JWT tokens. This service handles the creation, validation, and invalidation
 * of JWT tokens used for user authentication and session management.
 */
public interface JwtService {
  /**
   * Creates a new JWT token for a user.
   *
   * @param userId The ID of the user for whom to create the token
   * @return A signed JWT token string
   */
  String createToken(String userId);

  /**
   * Validates a JWT token.
   *
   * @param token The token to validate
   * @return The user ID associated with the token or null if the token is invalid
   */
  @Nullable String validateToken(String token);

  /**
   * Invalidates a JWT token, preventing its future use.
   *
   * @param token The token to invalidate
   * @return true if the token was successfully invalidated, false if the token is invalid
   */
  boolean invalidateToken(String token);
}
