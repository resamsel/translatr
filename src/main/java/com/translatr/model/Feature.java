package com.translatr.model;

import java.util.Arrays;
import java.util.Optional;

/**
 * The feature flags known to the application. {@code key} is the stable string stored in
 * {@code user_feature_flag.feature} / {@code feature_flag.feature} and sent to the frontend;
 * {@code defaultEnabled} is the hardcoded fallback used when neither a per-user override nor a
 * global {@link FeatureFlag} row exists. Keys are kept in sync with
 * {@code ui/libs/translatr-model/src/lib/model/feature.ts}.
 */
public enum Feature {
    ProjectCliCard    ("project-cli-card",    false),
    ProjectInfographic("project-infographic", false),
    HeaderGraphic     ("header-graphic",      false),
    LanguageSwitcher  ("language-switcher",   false);

    public final String  key;
    public final boolean defaultEnabled;

    Feature(String key, boolean defaultEnabled) {
        this.key            = key;
        this.defaultEnabled = defaultEnabled;
    }

    public static Optional<Feature> of(String key) {
        return Arrays.stream(values()).filter(f -> f.key.equals(key)).findFirst();
    }
}
