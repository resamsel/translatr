package com.translatr.mapper;

import com.translatr.dto.*;
import com.translatr.model.*;
import com.translatr.util.EmailUtils;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DtoMapper {

    public UserDto toDto(User u) {
        if (u == null) return null;
        UserDto d = new UserDto();
        d.id              = u.id;
        d.whenCreated     = u.whenCreated;
        d.whenUpdated     = u.whenUpdated;
        d.name            = u.name;
        d.username        = u.username;
        d.email           = u.email;
        d.emailHash       = EmailUtils.hashEmail(u.email);
        d.role            = u.role != null ? u.role.name() : null;
        d.preferredLocale = u.preferredLocale;
        d.settings        = u.settings;
        return d;
    }

    public ProjectDto toDto(Project p) {
        if (p == null) return null;
        ProjectDto d = new ProjectDto();
        d.id           = p.id;
        d.whenCreated  = p.whenCreated;
        d.whenUpdated  = p.whenUpdated;
        d.name         = p.name;
        d.description  = p.description;
        d.wordCount    = p.wordCount;
        if (p.owner != null) {
            d.ownerId         = p.owner.id;
            d.ownerName       = p.owner.name;
            d.ownerUsername   = p.owner.username;
            d.ownerEmailHash  = EmailUtils.hashEmail(p.owner.email);
        }
        return d;
    }

    public KeyDto toDto(Key k) {
        if (k == null) return null;
        KeyDto d = new KeyDto();
        d.id          = k.id;
        d.whenCreated = k.whenCreated;
        d.whenUpdated = k.whenUpdated;
        d.name        = k.name;
        d.wordCount   = k.wordCount;
        if (k.project != null) {
            d.projectId   = k.project.id;
            d.projectName = k.project.name;
            if (k.project.owner != null) {
                d.projectOwnerUsername = k.project.owner.username;
            }
        }
        return d;
    }

    public LocaleDto toDto(Locale l) {
        if (l == null) return null;
        LocaleDto d = new LocaleDto();
        d.id          = l.id;
        d.whenCreated = l.whenCreated;
        d.whenUpdated = l.whenUpdated;
        d.name        = l.name;
        d.wordCount   = l.wordCount;
        if (l.project != null) {
            d.projectId   = l.project.id;
            d.projectName = l.project.name;
            if (l.project.owner != null) {
                d.projectOwnerUsername = l.project.owner.username;
            }
        }
        return d;
    }

    public MessageDto toDto(Message m) {
        if (m == null) return null;
        MessageDto d = new MessageDto();
        d.id          = m.id;
        d.whenCreated = m.whenCreated;
        d.whenUpdated = m.whenUpdated;
        d.value       = m.value;
        d.wordCount   = m.wordCount;
        if (m.locale != null) {
            d.localeId   = m.locale.id;
            d.localeName = m.locale.name;
            if (m.locale.project != null) {
                d.projectId   = m.locale.project.id;
                d.projectName = m.locale.project.name;
            }
        }
        if (m.key != null) {
            d.keyId   = m.key.id;
            d.keyName = m.key.name;
        }
        return d;
    }

    public AccessTokenDto toDto(AccessToken t) {
        if (t == null) return null;
        AccessTokenDto d = new AccessTokenDto();
        d.id          = t.id;
        d.whenCreated = t.whenCreated;
        d.whenUpdated = t.whenUpdated;
        d.name        = t.name;
        d.key         = t.key;
        d.scope       = t.scope;
        if (t.user != null) {
            d.userId      = t.user.id;
            d.userUsername = t.user.username;
        }
        return d;
    }

    public MemberDto toDto(ProjectUser pu) {
        if (pu == null) return null;
        MemberDto d = new MemberDto();
        d.id          = pu.id;
        d.whenCreated = pu.whenCreated;
        d.role        = pu.role != null ? pu.role.name() : null;
        if (pu.project != null) {
            d.projectId   = pu.project.id;
            d.projectName = pu.project.name;
        }
        if (pu.user != null) {
            d.userId       = pu.user.id;
            d.userUsername = pu.user.username;
            d.userName     = pu.user.name;
            d.userEmailHash = EmailUtils.hashEmail(pu.user.email);
        }
        return d;
    }

    public ActivityDto toDto(LogEntry e) {
        if (e == null) return null;
        ActivityDto d = new ActivityDto();
        d.id          = e.id;
        d.type        = e.type != null ? e.type.name() : null;
        d.contentType = e.contentType;
        d.whenCreated = e.whenCreated;
        d.before      = e.before;
        d.after       = e.after;
        if (e.user != null) {
            d.userId      = e.user.id;
            d.userName    = e.user.name;
            d.userUsername = e.user.username;
        }
        if (e.project != null) {
            d.projectId   = e.project.id;
            d.projectName = e.project.name;
        }
        return d;
    }

    public FeatureFlagDto toDto(UserFeatureFlag f) {
        if (f == null) return null;
        FeatureFlagDto d = new FeatureFlagDto();
        d.id          = f.id;
        d.whenCreated = f.whenCreated;
        d.feature     = f.feature;
        d.enabled     = f.enabled;
        if (f.user != null) d.userId = f.user.id;
        return d;
    }
}
