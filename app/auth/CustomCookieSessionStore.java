package auth;

import com.typesafe.config.Config;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.oidc.profile.OidcProfile;
import org.pac4j.oidc.profile.OidcProfileDefinition;
import org.pac4j.play.store.PlayCookieSessionStore;
import org.pac4j.play.store.ShiroAesDataEncrypter;
import utils.ConfigKey;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.LinkedHashMap;

@Singleton
public class CustomCookieSessionStore extends PlayCookieSessionStore {
  static final char PADDING_CHAR = '*';

  @Inject
  public CustomCookieSessionStore(final Config configuration) {
    super(new ShiroAesDataEncrypter(provideEncryptionKey(configuration)));
  }

  static byte[] provideEncryptionKey(Config configuration) {
    String key = ConfigKey.PlayHttpSecretKey.getOrDefault(configuration, "");

    if (key.length() > 32) {
      return key.substring(0, 32).getBytes();
    }

    if (key.length() > 24) {
      return StringUtils.rightPad(key, 32, PADDING_CHAR).getBytes();
    }

    if (key.length() > 16) {
      return StringUtils.rightPad(key, 24, PADDING_CHAR).getBytes();
    }

    return StringUtils.rightPad(key, 16, PADDING_CHAR).getBytes();
  }
}
