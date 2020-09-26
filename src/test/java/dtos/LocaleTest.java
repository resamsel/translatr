package dtos;

import dto.Locale;
import mappers.LocaleMapper;
import mappers.MessageMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import play.mvc.Http;
import tests.AbstractLocaleTest;
import utils.FormatUtils;

import static assertions.CustomAssertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LocaleTest extends AbstractLocaleTest {
  @Mock
  private FormatUtils formatUtils;
  @Mock
  private MessageMapper messageMapper;

  private LocaleMapper target;

  @Before
  public void setUp() {
    target = new LocaleMapper(formatUtils, messageMapper);
  }

  @Test
  public void testDisplayName() {
    // given
    Http.Request request = mock(Http.Request.class);
    models.Locale model = new models.Locale();
    model.name = "de";

    when(formatUtils.formatDisplayName(eq(model), eq(request))).thenReturn("Deutsch");

    // when
    Locale actual = target.toDto(model, request);

    // then
    assertThat(actual).displayNameIsEqualTo("Deutsch");
  }
}
