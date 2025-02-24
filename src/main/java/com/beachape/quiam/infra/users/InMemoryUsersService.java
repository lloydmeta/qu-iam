package com.beachape.quiam.infra.users;

import static com.beachape.quiam.domain.users.UsersService.*;

import com.beachape.quiam.domain.users.UsersService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Default
@ApplicationScoped
public class InMemoryUsersService implements UsersService {

  private final PasswordHasher hasher = new PasswordHasher();

  private final Map<String, User> users = new ConcurrentHashMap<>();

  @Override
  public void upsert(UpsertUser newUser) {
    String passwordHash = hasher.hashPassword(newUser.password());
    User user = new User(newUser.name(), passwordHash);
    users.put(user.name(), user);
  }

  @Override
  public User authenticate(String name, String password) throws NoSuchUser, InvalidPassword {
    User retrievedUser = users.get(name);
    if (retrievedUser == null) {
      throw new NoSuchUser();
    } else {
      boolean passwordOk = hasher.verifyPassword(password, retrievedUser.passwordHash());
      if (passwordOk) {
        return retrievedUser;
      } else {
        throw new InvalidPassword();
      }
    }
  }
}
