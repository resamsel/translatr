package utils;

import com.google.common.collect.ImmutableMap;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import models.AccessToken;
import models.LinkedAccount;
import models.User;
import org.apache.http.client.utils.URIBuilder;
import play.mvc.Call;
import play.mvc.Http.RequestBuilder;

/**
 * Created by resamsel on 24/07/2017.
 */
public class TestFactory {

  public static Map<String, String> createSession(User user) {
    Optional<LinkedAccount> linkedAccountOptional = user.linkedAccounts.stream().findFirst();
    if (!linkedAccountOptional.isPresent()) {
      throw new IllegalArgumentException("User has no linked accounts");
    }

    LinkedAccount linkedAccount = linkedAccountOptional.get();
    return createSession(linkedAccount.providerKey, linkedAccount.providerUserId,
        UUID.randomUUID().toString());
  }

  public static Map<String, String> createSession(String providerKey, String providerId,
      String sessionKey) {
    return ImmutableMap.of(
        "pa.u.exp", "" + (System.currentTimeMillis() + 3600 * 1000),
        "pa.p.id", providerKey,
        "pa.u.id", providerId,
        "pa.s.id", sessionKey
    );
  }

  public static RequestBuilder requestAsJohnSmith() {
    return new RequestBuilder().session(createSession("google", "123916278356185", "asdfasdf"));
  }

  public static RequestBuilder requestAs(User user) {
    return new RequestBuilder().session(createSession(user));
  }

  public static RequestBuilder requestAs(Call call, AccessToken accessToken) {
    try {
      return new RequestBuilder()
          .method(call.method())
          .uri(
              new URIBuilder(call.url())
                  .addParameter("access_token", accessToken.key)
                  .build()
          );
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
