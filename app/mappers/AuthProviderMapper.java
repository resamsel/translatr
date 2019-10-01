package mappers;

import dto.AuthProvider;

public class AuthProviderMapper {
  public static AuthProvider toDto(com.feth.play.module.pa.providers.AuthProvider in) {
    AuthProvider out = new AuthProvider();

    out.key = in.getKey();
    out.url = in.getUrl();

    return out;
  }
}
