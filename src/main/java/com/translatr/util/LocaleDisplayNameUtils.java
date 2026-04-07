package com.translatr.util;

/**
 * Locale display-name utilities (replaces the Play-era FormatUtils.formatDisplayName).
 */
public final class LocaleDisplayNameUtils {

    private LocaleDisplayNameUtils() {}

    /**
     * Returns the display name of {@code localeName} (a BCP-47 tag, e.g. {@code "de"})
     * rendered in the given {@code displayLocale}.
     *
     * @return {@code null} if {@code localeName} is blank
     */
    public static String formatDisplayName(String localeName, java.util.Locale displayLocale) {
        if (localeName == null || localeName.isBlank()) {
            return null;
        }
        java.util.Locale locale = java.util.Locale.forLanguageTag(localeName);
        return displayLocale != null
                ? locale.getDisplayName(displayLocale)
                : locale.getDisplayName();
    }
}

