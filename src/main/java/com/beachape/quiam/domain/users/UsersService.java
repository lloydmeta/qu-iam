package com.beachape.quiam.domain.users;

public interface UsersService {

  /** Upserts a user */
  public void upsert(UpsertUser user);

  /** Returns a user if a user exists with the given name and the password maches */
  public User authenticate(String name, String password) throws NoSuchUser, InvalidPassword;

  // Domain errors
  public class NoSuchUser extends RuntimeException {}
  ;

  public class InvalidPassword extends RuntimeException {}
  ;
}
