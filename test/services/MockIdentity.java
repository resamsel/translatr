package services;

import com.feth.play.module.pa.user.AuthUserIdentity;
import com.feth.play.module.pa.user.EmailIdentity;
import com.feth.play.module.pa.user.NameIdentity;

public class MockIdentity implements AuthUserIdentity, EmailIdentity, NameIdentity {

  private final String id;
  private final String provider;
  private final String name;
  private final String email;

  MockIdentity(String id, String provider, String name, String email) {
    this.id = id;
    this.provider = provider;
    this.name = name;
    this.email = email;
  }

  @Override
  public String getEmail() {
    return email;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getProvider() {
    return provider;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    services.MockIdentity that = (services.MockIdentity) o;

    return id != null ? id.equals(that.id) : that.id == null;
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }
}
