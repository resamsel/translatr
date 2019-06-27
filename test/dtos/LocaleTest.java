package dtos;

import assertions.Assertions;
import mappers.LocaleMapper;
import org.junit.Test;
import tests.AbstractLocaleTest;

public class LocaleTest extends AbstractLocaleTest {

  @Test
  public void testDisplayName() {
    java.util.Locale.setDefault(java.util.Locale.forLanguageTag("de"));

    models.Locale model = new models.Locale();
    model.name = "de";
    Assertions.assertThat(LocaleMapper.toDto(model)).displayNameIsEqualTo("Deutsch");
  }
}
