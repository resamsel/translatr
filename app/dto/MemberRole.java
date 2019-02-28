package dto;

import models.ProjectRole;

public enum MemberRole {
    Owner, Manager, Developer, Translator;

    public static MemberRole from(ProjectRole role) {
        return MemberRole.valueOf(role.name());
    }
}
