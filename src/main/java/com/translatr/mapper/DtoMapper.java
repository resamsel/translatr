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
        ProjectDto d = new ProjectDto()
                .id(p.id)
                .whenCreated(toOffsetDateTime(p.whenCreated))
                .whenUpdated(toOffsetDateTime(p.whenUpdated))
                .name(p.name)
                .description(p.description)
                .wordCount(p.wordCount);
        if (p.owner != null) {
            d.setOwnerId(p.owner.id);
            d.setOwnerName(p.owner.name);
            d.setOwnerUsername(p.owner.username);
            d.setOwnerEmailHash(EmailUtils.hashEmail(p.owner.email));
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

    /**
     * A just-persisted entity (mapped back right after {@code repo.persist()} in a
     * {@code *Service.create}) can still have a null {@code whenCreated}/{@code whenUpdated}
     * here: {@code @CreationTimestamp}/{@code @UpdateTimestamp} are populated by Hibernate at
     * flush time, which has not happened yet. All six {@code toDto} methods pass through here,
     * so a null Instant maps to a null OffsetDateTime rather than throwing.
     */
    private static java.time.OffsetDateTime toOffsetDateTime(java.time.Instant i) {
        return i == null ? null : i.atOffset(java.time.ZoneOffset.UTC);
    }

    public MessageDto toDto(Message m) {
        if (m == null) return null;
        MessageDto d = new MessageDto()
                .id(m.id)
                .whenCreated(toOffsetDateTime(m.whenCreated))
                .whenUpdated(toOffsetDateTime(m.whenUpdated))
                .value(m.value)
                .wordCount(m.wordCount);
        if (m.locale != null) {
            d.setLocaleId(m.locale.id);
            d.setLocaleName(m.locale.name);
            if (m.locale.project != null) {
                d.setProjectId(m.locale.project.id);
                d.setProjectName(m.locale.project.name);
            }
        }
        if (m.key != null) {
            d.setKeyId(m.key.id);
            d.setKeyName(m.key.name);
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
