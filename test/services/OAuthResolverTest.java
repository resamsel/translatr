package services;

import org.junit.Test;
import play.Configuration;
import play.api.mvc.Call;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OAuthResolverTest {

  @Test
  public void adaptRedirectUriWithRelativeUriAndNoSSL() {
    // given
    String redirectUri = "/authenticate/keycloak";
    Configuration configuration = mock(Configuration.class);

    when(configuration.getBoolean(anyString())).thenReturn(false);

    // when
    String actual = OAuthResolver.adaptRedirectUri(redirectUri, configuration);

    // then
    assertThat(actual).isEqualTo(redirectUri);
  }

  @Test
  public void adaptRedirectUriWithRelativeUriAndSSL() {
    // given
    String redirectUri = "/authenticate/keycloak";
    Configuration configuration = mock(Configuration.class);

    when(configuration.getBoolean(anyString())).thenReturn(true);

    // when
    String actual = OAuthResolver.adaptRedirectUri(redirectUri, configuration);

    // then
    assertThat(actual).isEqualTo(redirectUri);
  }

  @Test
  public void adaptRedirectUriWithAbsoluteUriAndNoSSL() {
    // given
    String redirectUri = "http://translatr.repanzar.com/authenticate/keycloak";
    Configuration configuration = mock(Configuration.class);

    when(configuration.getBoolean(anyString())).thenReturn(false);

    // when
    String actual = OAuthResolver.adaptRedirectUri(redirectUri, configuration);

    // then
    assertThat(actual).isEqualTo(redirectUri);
  }

  @Test
  public void adaptRedirectUriWithAbsoluteUriAndSSL() {
    // given
    String redirectUri = "http://translatr.repanzar.com/authenticate/keycloak";
    Configuration configuration = mock(Configuration.class);

    when(configuration.getBoolean(anyString())).thenReturn(true);

    // when
    String actual = OAuthResolver.adaptRedirectUri(redirectUri, configuration);

    // then
    assertThat(actual).isEqualTo("https://translatr.repanzar.com/authenticate/keycloak");
  }

  @Test
  public void adaptRedirectCallWithAbsoluteUriAndSSL() {
    // given
    Call redirectCall = new Call("GET", "http://translatr.repanzar.com/authenticate/keycloak", null);
    Configuration configuration = mock(Configuration.class);

    when(configuration.getBoolean(anyString())).thenReturn(true);

    // when
    Call actual = OAuthResolver.adaptRedirectCall(redirectCall, configuration);

    // then
    assertThat(actual.url()).isEqualTo("https://translatr.repanzar.com/authenticate/keycloak");
  }
}
