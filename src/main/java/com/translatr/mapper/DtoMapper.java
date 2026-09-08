package com.translatr.mapper;

import com.translatr.dto.*;
import com.translatr.model.*;
import com.translatr.util.EmailUtils;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DtoMapper {

    public UserDto toDto(User u) {
        if (u == null) return null;
        return new UserDto()
                .id(u.id)
                .whenCreated(toOffsetDateTime(u.whenCreated))
                .whenUpdated(toOffsetDateTime(u.whenUpdated))
                .name(u.name)
                .username(u.username)
                .email(u.email)
                .emailHash(EmailUtils.hashEmail(u.email))
                .role(u.role != null ? u.role.name() : null)
                .preferredLocale(u.preferredLocale)
                .settings(u.settings)
                // features is attached later by UserService for single-user reads; keep it
                // absent (not an empty map) on list responses, matching the old hand DTO.
                .features(null);
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
        KeyDto d = new KeyDto()
                .id(k.id)
                .whenCreated(toOffsetDateTime(k.whenCreated))
                .whenUpdated(toOffsetDateTime(k.whenUpdated))
                .name(k.name)
                .wordCount(k.wordCount);
        if (k.project != null) {
            d.setProjectId(k.project.id);
            d.setProjectName(k.project.name);
            if (k.project.owner != null) {
                d.setProjectOwnerUsername(k.project.owner.username);
            }
        }
        return d;
    }

    public LocaleDto toDto(Locale l) {
        if (l == null) return null;
        LocaleDto d = new LocaleDto()
                .id(l.id)
                .whenCreated(toOffsetDateTime(l.whenCreated))
                .whenUpdated(toOffsetDateTime(l.whenUpdated))
                .name(l.name)
                .wordCount(l.wordCount);
        if (l.project != null) {
            d.setProjectId(l.project.id);
            d.setProjectName(l.project.name);
            if (l.project.owner != null) {
                d.setProjectOwnerUsername(l.project.owner.username);
            }
        }
        return d;
    }

    private static java.time.OffsetDateTime toOffsetDateTime(java.time.Instant i) {
        return i == null ? null : i.atOffset(java.time.ZoneOffset.UTC);
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
        AccessTokenDto d = new AccessTokenDto()
                .id(t.id)
                .whenCreated(toOffsetDateTime(t.whenCreated))
                .whenUpdated(toOffsetDateTime(t.whenUpdated))
                .name(t.name)
                .key(t.key)
                .scope(t.scope);
        if (t.user != null) {
            d.setUserId(t.user.id);
            d.setUserUsername(t.user.username);
        }
        return d;
    }

    public MemberDto toDto(ProjectUser pu) {
        if (pu == null) return null;
        MemberDto d = new MemberDto()
                .id(pu.id)
                .whenCreated(toOffsetDateTime(pu.whenCreated))
                .role(pu.role != null ? pu.role.name() : null);
        if (pu.project != null) {
            d.setProjectId(pu.project.id);
            d.setProjectName(pu.project.name);
        }
        if (pu.user != null) {
            d.setUserId(pu.user.id);
            d.setUserUsername(pu.user.username);
            d.setUserName(pu.user.name);
            d.setUserEmailHash(EmailUtils.hashEmail(pu.user.email));
        }
        return d;
    }

    public ActivityDto toDto(LogEntry e) {
        if (e == null) return null;
        ActivityDto d = new ActivityDto()
                .id(e.id)
                .type(e.type != null ? e.type.name() : null)
                .contentType(e.contentType)
                .whenCreated(toOffsetDateTime(e.whenCreated))
                .before(e.before)
                .after(e.after);
        if (e.user != null) {
            d.setUserId(e.user.id);
            d.setUserName(e.user.name);
            d.setUserUsername(e.user.username);
        }
        if (e.project != null) {
            d.setProjectId(e.project.id);
            d.setProjectName(e.project.name);
        }
        return d;
    }

    public FeatureFlagDto toDto(UserFeatureFlag f) {
        if (f == null) return null;
        return new FeatureFlagDto()
                .id(f.id)
                .whenCreated(toOffsetDateTime(f.whenCreated))
                .userId(f.user != null ? f.user.id : null)
                .feature(f.feature)
                .enabled(f.enabled);
    }

    public GlobalFeatureFlagDto toDto(FeatureFlag f) {
        if (f == null) return null;
        return new GlobalFeatureFlagDto()
                .id(f.id)
                .whenCreated(toOffsetDateTime(f.whenCreated))
                .feature(f.feature)
                .enabled(f.enabled);
    }
}
