package utils;

import com.translatr.util.LocaleDisplayNameUtils;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link LocaleDisplayNameUtils} — replaces the Play-era FormatUtils.formatDisplayName tests.
 * No mocking needed: the logic is pure Java {@link java.util.Locale}.
 */
class FormatUtilsTest {

    @Test
    void formatDisplayName_null_returnsNull() {
        assertThat(LocaleDisplayNameUtils.formatDisplayName(null, Locale.ENGLISH)).isNull();
    }

    @Test
    void formatDisplayName_empty_returnsNull() {
        assertThat(LocaleDisplayNameUtils.formatDisplayName("", Locale.ENGLISH)).isNull();
    }

    @Test
    void formatDisplayName_blank_returnsNull() {
        assertThat(LocaleDisplayNameUtils.formatDisplayName("   ", Locale.ENGLISH)).isNull();
    }

    @Test
    void formatDisplayName_enLocale_displayedInEnglish() {
        assertThat(LocaleDisplayNameUtils.formatDisplayName("en", Locale.ENGLISH))
                .isEqualTo("English");
    }

    @Test
    void formatDisplayName_deLocale_displayedInEnglish() {
        assertThat(LocaleDisplayNameUtils.formatDisplayName("de", Locale.ENGLISH))
                .isEqualTo("German");
    }

    @Test
    void formatDisplayName_deLocale_displayedInGerman() {
        assertThat(LocaleDisplayNameUtils.formatDisplayName("de", Locale.GERMAN))
                .isEqualTo("Deutsch");
    }

    @Test
    void formatDisplayName_enLocale_displayedInGerman() {
        assertThat(LocaleDisplayNameUtils.formatDisplayName("en", Locale.GERMAN))
                .isEqualTo("Englisch");
    }

    @Test
    void formatDisplayName_nullDisplayLocale_usesDefaultLocale() {
        // When displayLocale is null, java.util.Locale.getDisplayName() is called
        String result = LocaleDisplayNameUtils.formatDisplayName("en", null);
        assertThat(result).isNotBlank();
    }
}
