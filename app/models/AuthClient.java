package models;

public class AuthClient {
  public String key;
  public String url;

  public static AuthClient of(String key, String url) {
    AuthClient out = new AuthClient();

    out.key = key;
    out.url = url;

    return out;
  }
}
