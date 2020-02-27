package models;

import com.avaje.ebean.annotation.DbEnumValue;

public enum FeatureFlag {
    ProjectCliCard("project-cli-card", false);

    private final String name;
    private final boolean enabled;

    FeatureFlag(String name, boolean enabled) {
        this.name = name;
        this.enabled = enabled;
    }

    @DbEnumValue
    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
