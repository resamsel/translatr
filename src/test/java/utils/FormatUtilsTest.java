package utils;

import models.Locale;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import play.i18n.MessagesApi;
import play.mvc.Http;
import tests.AbstractLocaleTest;

import static org.assertj.core.api.Assertions.assertThat;

public class FormatUtilsTest extends AbstractLocaleTest {
  private FormatUtils formatUtils;
  private MessagesApi messagesApi;

  @Before
  public void setUp() {
    messagesApi = Mockito.mock(MessagesApi.class);
    formatUtils = new FormatUtils(messagesApi);
  }

  @Test
  public void testFormatEnglishDisplayNames() {
    // given
    Http.Request request = Mockito.mock(Http.Request.class);

    java.util.Locale.setDefault(java.util.Locale.forLanguageTag("en"));

    assertThat(formatUtils.formatDisplayName(createLocale(null), request)).isNull();
    assertThat(formatUtils.formatDisplayName(createLocale(""), request)).isNull();
    assertThat(formatUtils.formatDisplayName(createLocale("de"), request)).isEqualTo("German");
    assertThat(formatUtils.formatDisplayName(createLocale("en"), request)).isEqualTo("English");
  }

  @Test
  public void testFormatGermanDisplayNames() {
    // given
    Http.Request request = Mockito.mock(Http.Request.class);
    java.util.Locale.setDefault(java.util.Locale.forLanguageTag("de"));

    // when, then
    assertThat(formatUtils.formatDisplayName(createLocale(null), request)).isNull();
    assertThat(formatUtils.formatDisplayName(createLocale(""), request)).isNull();
    assertThat(formatUtils.formatDisplayName(createLocale("de"), request)).isEqualTo("Deutsch");
    assertThat(formatUtils.formatDisplayName(createLocale("en"), request)).isEqualTo("Englisch");
  }

  private Locale createLocale(String name) {
    Locale locale = new Locale();
    locale.name = name;
    return locale;
  }
}
