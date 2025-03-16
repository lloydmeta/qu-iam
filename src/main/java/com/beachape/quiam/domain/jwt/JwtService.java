package com.beachape.quiam.domain.jwt;

import com.beachape.quiam.domain.errors.DomainException;

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
   * @return The user ID associated with the token
   * @throws TokenValidationException if the token is invalid or expired
   */
  String validateToken(String token) throws TokenValidationException;

  /**
   * Invalidates a JWT token, preventing its future use.
   *
   * @param token The token to invalidate
   * @throws TokenValidationException if the token is invalid or cannot be invalidated
   */
  void invalidateToken(String token) throws TokenValidationException;

  public class TokenValidationException extends DomainException {
    public TokenValidationException(String message) {
      super(message);
    }

    public TokenValidationException(Throwable cause) {
      super("Failed to validate token", cause);
    }
  }
}
