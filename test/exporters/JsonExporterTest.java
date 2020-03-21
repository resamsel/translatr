package exporters;

import models.Key;
import models.Locale;
import models.Message;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonExporterTest {

  private JsonExporter target;

  @Before
  public void setUp() {
    target = new JsonExporter();
  }

  @Test
  public void applyWithNullValue() {
    // given
    Locale locale = null;

    // when
    byte[] actual = target.apply(locale);

    // then
    assertThat(actual).isEqualTo(new byte[]{});
  }

  @Test
  public void applyWithNullMessages() {
    // given
    Locale locale = new Locale();

    // when
    byte[] actual = target.apply(locale);

    // then
    assertThat(actual).isEqualTo(new byte[]{});
  }

  @Test
  public void applyWithNoMessages() {
    // given
    Locale locale = new Locale();
    locale.messages = Collections.emptyList();

    // when
    byte[] actual = target.apply(locale);

    // then
    assertThat(actual).isEqualTo("{}".getBytes());
  }

  @Test
  public void applyWithSingleMessage() {
    // given
    Locale locale = new Locale();
    Message message = new Message();
    message.key = new Key();
    message.key.name = "a";
    message.value = "1";
    locale.messages = Collections.singletonList(message);

    // when
    byte[] actual = target.apply(locale);

    // then
    assertThat(actual).isEqualTo("{\"a\":\"1\"}".getBytes());
  }

  @Test
  public void applyWithMultipleMessage() {
    // given
    Locale locale = new Locale();
    locale.messages = Stream.of("c", "b", "a")
        .map(keyName -> {
          Message message = new Message();
          message.key = new Key();
          message.key.name = keyName;
          message.value = keyName + "1";
          return message;
        })
        .collect(Collectors.toList());

    // when
    byte[] actual = target.apply(locale);

    // then
    assertThat(actual).isEqualTo("{\"a\":\"a1\",\"b\":\"b1\",\"c\":\"c1\"}".getBytes());
  }
}
