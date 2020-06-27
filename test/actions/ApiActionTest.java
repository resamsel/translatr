package actions;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import play.i18n.Lang;
import play.mvc.Http;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import static org.mockito.Mockito.*;

public class ApiActionTest {

  @Test
  public void changeLangFromHeaderWithNoHeaders() {
    // given
    Http.Context ctxSpy = spy(new Http.Context(1L, null, null, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap()));
    Map<String, String[]> headers = Collections.emptyMap();

    // when
    ApiAction.changeLangFromHeader(ctxSpy, headers);

    // then
    verifyZeroInteractions(ctxSpy);
  }

  @Test
  public void changeLangFromHeaderWithHeaderLanguageEqualsContextLanguage() {
    // given
    Http.Context ctxSpy = spy(new Http.Context(1L, null, null, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap()));
    Map<String, String[]> headers = ImmutableMap.of("accept-language", new String[]{"en"});

    doReturn(new Lang(new Locale("en"))).when(ctxSpy).lang();

    // when
    ApiAction.changeLangFromHeader(ctxSpy, headers);

    // then
    verify(ctxSpy, times(1)).lang();
    verifyNoMoreInteractions(ctxSpy);
  }

  @Test
  public void changeLangFromHeaderWithHeaderLanguageDifferingContextLanguage() {
    // given
    Http.Context ctxSpy = spy(new Http.Context(1L, null, null, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap()));
    Map<String, String[]> headers = ImmutableMap.of("accept-language", new String[]{"de"});

    doReturn(new Lang(new Locale("en"))).when(ctxSpy).lang();
    doReturn(true).when(ctxSpy).changeLang(eq("de"));

    // when
    ApiAction.changeLangFromHeader(ctxSpy, headers);

    // then
    verify(ctxSpy, times(1)).lang();
    verify(ctxSpy, times(1)).changeLang(eq("de"));
    verifyNoMoreInteractions(ctxSpy);
  }
}
