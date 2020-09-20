package auth;

import com.nimbusds.oauth2.sdk.pkce.CodeVerifier;
import org.pac4j.play.PlayWebContext;
import org.pac4j.play.store.NoOpDataEncrypter;
import org.pac4j.play.store.PlayCookieSessionStore;

import javax.inject.Singleton;

@Singleton
public class CustomCookieSessionStore extends PlayCookieSessionStore {

  public CustomCookieSessionStore() {
    super(new NoOpDataEncrypter());
  }

  @Override
  public void set(PlayWebContext context, String key, Object value) {
    Object cleanedValue = value;
    if (key.endsWith("$codeVerifierSessionParameter") && value instanceof CodeVerifier) {
      cleanedValue = CustomCodeVerifier.from((CodeVerifier) value);
    }

    super.set(context, key, cleanedValue);
  }
}
