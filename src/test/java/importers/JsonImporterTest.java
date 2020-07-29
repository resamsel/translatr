package importers;

import com.fasterxml.jackson.databind.JsonMappingException;
import models.Locale;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import services.KeyService;
import services.MessageService;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@RunWith(MockitoJUnitRunner.class)
public class JsonImporterTest {

  @Mock
  private KeyService keyService;
  @Mock
  private MessageService messageService;

  private JsonImporter target;

  @Before
  public void setUp() {
    target = new JsonImporter(keyService, messageService);
  }

  @Test
  public void retrievePropertiesWithNoJsonObject() {
    // given
    InputStream inputStream = new ByteArrayInputStream("".getBytes());
    Locale locale = new Locale();

    // when
    Throwable thrown = catchThrowable(() -> target.retrieveProperties(inputStream, locale));

    // then
    assertThat(thrown)
        .isInstanceOf(JsonMappingException.class)
        .hasMessageContaining("end-of-input");
  }

  @Test
  public void retrievePropertiesWithEmptyJsonObject() throws Exception {
    // given
    InputStream inputStream = new ByteArrayInputStream("{}".getBytes());
    Locale locale = new Locale();

    // when
    Properties actual = target.retrieveProperties(inputStream, locale);

    // then
    assertThat(actual).hasSize(0);
  }

  @Test
  public void retrievePropertiesWithSingleMessageJsonObject() throws Exception {
    // given
    InputStream inputStream = new ByteArrayInputStream("{\"a\":\"1\"}".getBytes());
    Locale locale = new Locale();

    // when
    Properties actual = target.retrieveProperties(inputStream, locale);

    // then
    assertThat(actual)
        .hasSize(1)
        .containsEntry("a", "1");
  }

  @Test
  public void retrievePropertiesWithMultipleMessagesJsonObject() throws Exception {
    // given
    InputStream inputStream = new ByteArrayInputStream("{\"a\":\"1\",\"b\":\"2\",\"c\":\"3\"}".getBytes());
    Locale locale = new Locale();

    // when
    Properties actual = target.retrieveProperties(inputStream, locale);

    // then
    assertThat(actual)
        .hasSize(3)
        .containsEntry("a", "1")
        .containsEntry("b", "2")
        .containsEntry("c", "3");
  }
}
