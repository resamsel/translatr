package auth;

import com.nimbusds.oauth2.sdk.pkce.CodeVerifier;

import java.io.Serializable;

public class CustomCodeVerifier extends CodeVerifier implements Serializable {
  public CustomCodeVerifier(String value) {
    super(value);
  }

  public static CustomCodeVerifier from(CodeVerifier codeVerifier) {
    return new CustomCodeVerifier(codeVerifier.getValue());
  }
}
