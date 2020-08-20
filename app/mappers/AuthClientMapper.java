package mappers;

import dto.AuthClient;

public class AuthClientMapper {
  public static AuthClient toDto(models.AuthClient in) {
    AuthClient out = new AuthClient();

    out.key = in.key;
    out.url = in.url;

    return out;
  }
}
