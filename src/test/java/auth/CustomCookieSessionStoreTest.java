package auth;

import com.typesafe.config.Config;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import utils.ConfigKey;

import java.util.Arrays;
import java.util.Collection;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class CustomCookieSessionStoreTest {

  private final String input;
  private final String expected;

  public CustomCookieSessionStoreTest(String input, String expected) {
    this.input = input;
    this.expected = expected;
  }

  @Parameterized.Parameters
  public static Collection<String[]> parameters() {
    return Arrays.asList(new String[][] {
            {null, "****************"},
            {"", "****************"},
            {"a", "a***************"},
            {"12345678", "12345678********"},
            {"123456789012345", "123456789012345*"},
            {"1234567890123456", "1234567890123456"},
            {"12345678901234567", "12345678901234567*******"},
            {"12345678901234567890123", "12345678901234567890123*"},
            {"123456789012345678901234", "123456789012345678901234"},
            {"1234567890123456789012345", "1234567890123456789012345*******"},
            {"1234567890123456789012345678901", "1234567890123456789012345678901*"},
            {"12345678901234567890123456789012", "12345678901234567890123456789012"},
            {"123456789012345678901234567890123", "12345678901234567890123456789012"}
    });
  }

  @Test
  public void provideEncryptionKey() {
    // given
    Config config = mock(Config.class);

    when(config.hasPath(eq(ConfigKey.PlayHttpSecretKey.key()))).thenReturn(input != null);
    when(config.getString(eq(ConfigKey.PlayHttpSecretKey.key()))).thenReturn(input);

    // when
    byte[] actual = CustomCookieSessionStore.provideEncryptionKey(config);

    // then
    Assertions.assertThat(actual).containsExactly(expected.getBytes());
  }
}
