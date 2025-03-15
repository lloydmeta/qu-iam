package com.beachape.quiam.infra.users;

import com.beachape.quiam.domain.users.UsersService;
import io.opentelemetry.instrumentation.annotations.WithSpan;
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
  @WithSpan
  public void upsert(UpsertUser newUser) {
    String passwordHash = hasher.hashPassword(newUser.password());
    User user = User.builder().name(newUser.username()).passwordHash(passwordHash).build();
    users.put(user.name(), user);
  }

  @Override
  @WithSpan
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
