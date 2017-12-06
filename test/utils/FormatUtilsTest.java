package utils;

import models.Locale;
import org.junit.Test;
import tests.AbstractLocaleTest;

import static org.fest.assertions.api.Assertions.assertThat;
import static utils.FormatUtils.formatDisplayName;

public class FormatUtilsTest extends AbstractLocaleTest {
    @Test
    public void testFormatEnglishDisplayNames() {
        java.util.Locale.setDefault(java.util.Locale.forLanguageTag("en"));

        assertThat(formatDisplayName(createLocale(null))).isNull();
        assertThat(formatDisplayName(createLocale(""))).isNull();
        assertThat(formatDisplayName(createLocale("de"))).isEqualTo("German");
        assertThat(formatDisplayName(createLocale("en"))).isEqualTo("English");
    }

    @Test
    public void testFormatGermanDisplayNames() {
        java.util.Locale.setDefault(java.util.Locale.forLanguageTag("de"));

        assertThat(formatDisplayName(createLocale(null))).isNull();
        assertThat(formatDisplayName(createLocale(""))).isNull();
        assertThat(formatDisplayName(createLocale("de"))).isEqualTo("Deutsch");
        assertThat(formatDisplayName(createLocale("en"))).isEqualTo("Englisch");
    }

    private Locale createLocale(String name) {
        Locale locale = new Locale();
        locale.name = name;
        return locale;
    }
}
