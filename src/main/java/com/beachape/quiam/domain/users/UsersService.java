package com.beachape.quiam.domain.users;

import com.beachape.quiam.domain.errors.DomainException;
import lombok.Builder;

/** Service interface for managing users. */
public interface UsersService {

  /** Upserts a user */
  public void upsert(UpsertUser user);

  /** Returns a user if a user exists with the given name and the password matches */
  public User authenticate(String name, String password) throws UsersServiceException;

  /** Domain errors for this service */
  public abstract static sealed class UsersServiceException extends DomainException
      permits NoSuchUser, InvalidPassword {}

  public static final class NoSuchUser extends UsersServiceException {}

  public static final class InvalidPassword extends UsersServiceException {}

  @Builder
  public static record User(String name, String passwordHash) {}

  @Builder
  public static record UpsertUser(String username, String password) {}
}
