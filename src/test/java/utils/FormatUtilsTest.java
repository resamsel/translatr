package utils;

import models.Locale;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import play.i18n.Lang;
import play.i18n.Messages;
import play.i18n.MessagesApi;
import play.mvc.Http;
import tests.AbstractLocaleTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FormatUtilsTest extends AbstractLocaleTest {
  @Mock
  private MessagesApi messagesApi;
  @Mock
  private Messages messages;
  @Mock
  private Lang lang;
  @Mock
  private Http.Request request;

  private FormatUtils target;

  @Before
  public void setUp() {
    target = new FormatUtils(messagesApi);

    when(messagesApi.preferred(eq(request))).thenReturn(messages);
    when(messages.lang()).thenReturn(lang);
  }

  @Test
  public void formatDisplayNamesWithEnglishLocaleAndNameNull() {
    // given, when
    String actual = target.formatDisplayName(createLocale(null), request);

    // then
    assertThat(actual).isNull();
  }

  @Test
  public void formatDisplayNamesWithEnglishLocaleAndNameEmpty() {
    // given, when
    String actual = target.formatDisplayName(createLocale(null), request);

    // then
    assertThat(target.formatDisplayName(createLocale(""), request)).isNull();
  }

  @Test
  public void formatDisplayNamesWithEnglishLocaleAndNameDe() {
    // given
    when(lang.locale()).thenReturn(java.util.Locale.forLanguageTag("en"));

    // when
    String actual = target.formatDisplayName(createLocale(null), request);

    // then
    assertThat(target.formatDisplayName(createLocale("en"), request)).isEqualTo("English");
  }

  @Test
  public void formatDisplayNamesWithEnglishLocaleAndNameEn() {
    // given
    when(lang.locale()).thenReturn(java.util.Locale.forLanguageTag("en"));

    // when
    String actual = target.formatDisplayName(createLocale(null), request);

    // then
    assertThat(target.formatDisplayName(createLocale("en"), request)).isEqualTo("English");
  }

  @Test
  public void formatDisplayNamesWithGermanLocaleAndNameNull() {
    // given, when
    String actual = target.formatDisplayName(createLocale(null), request);

    // then
    assertThat(actual).isNull();
  }

  @Test
  public void formatDisplayNamesWithGermanLocaleAndNameEmpty() {
    // given, when
    String actual = target.formatDisplayName(createLocale(null), request);

    // then
    assertThat(target.formatDisplayName(createLocale(""), request)).isNull();
  }

  @Test
  public void formatDisplayNamesWithGermanLocaleAndNameDe() {
    // given
    when(lang.locale()).thenReturn(java.util.Locale.forLanguageTag("de"));

    // when
    String actual = target.formatDisplayName(createLocale(null), request);

    // then
    assertThat(target.formatDisplayName(createLocale("de"), request)).isEqualTo("Deutsch");
  }

  @Test
  public void formatDisplayNamesWithGermanLocaleAndNameEn() {
    // given
    when(lang.locale()).thenReturn(java.util.Locale.forLanguageTag("de"));

    // when
    String actual = target.formatDisplayName(createLocale(null), request);

    // then
    assertThat(target.formatDisplayName(createLocale("en"), request)).isEqualTo("Englisch");
  }

  private Locale createLocale(String name) {
    Locale locale = new Locale();
    locale.name = name;
    return locale;
  }
}
