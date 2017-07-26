package utils;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import play.mvc.Http.RequestBuilder;

/**
 * Created by resamsel on 24/07/2017.
 */
public class TestFactory {

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
}
