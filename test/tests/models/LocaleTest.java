package tests.models;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.Test;

import models.Locale;

/**
 * @author resamsel
 * @version 18 May 2017
 */
public class LocaleTest {
  @Test
  public void getCacheKey() {
    UUID localeId = UUID.randomUUID();

    assertThat(Locale.getCacheKey(null)).isNull();
    assertThat(Locale.getCacheKey(localeId)).isEqualTo("locale:" + localeId);
    assertThat(Locale.getCacheKey(localeId, "project"))
        .isEqualTo("locale:" + localeId + ":project");
    assertThat(Locale.getCacheKey(localeId, "project", "messages"))
        .isEqualTo("locale:" + localeId + ":project:messages");
  }
}
