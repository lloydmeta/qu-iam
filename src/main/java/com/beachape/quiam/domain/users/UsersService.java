package com.beachape.quiam.domain.users;

import com.beachape.quiam.domain.errors.DomainException;
import lombok.Builder;

public interface UsersService {

  /** Upserts a user */
  public void upsert(UpsertUser user);

  /** Returns a user if a user exists with the given name and the password maches */
  public User authenticate(String name, String password) throws NoSuchUser, InvalidPassword;

  // Domain errors
  public static class NoSuchUser extends DomainException {}
  ;

  public static class InvalidPassword extends DomainException {}
  ;

  @Builder
  public static record User(String name, String passwordHash) {}

  @Builder
  public static record UpsertUser(String username, String password) {}
}
