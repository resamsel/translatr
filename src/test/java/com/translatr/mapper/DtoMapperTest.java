package com.translatr.mapper;

import com.translatr.dto.*;
import com.translatr.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link DtoMapper} — no CDI context required.
 */
class DtoMapperTest {

    private DtoMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new DtoMapper();
    }

    // -------------------------------------------------------------------------
    // User
    // -------------------------------------------------------------------------

    @Test
    void toDto_user_returnsNull_forNullInput() {
        assertThat(mapper.toDto((User) null)).isNull();
    }

    @Test
    void toDto_user_mapsAllFields() {
        User u = new User();
        u.id             = UUID.randomUUID();
        u.name           = "John";
        u.username       = "john";
        u.email          = "john@example.com";
        u.role           = UserRole.User;
        u.preferredLocale = "en";
        u.settings       = Map.of("theme", "dark");

        UserDto dto = mapper.toDto(u);

        assertThat(dto.id).isEqualTo(u.id);
        assertThat(dto.name).isEqualTo("John");
        assertThat(dto.username).isEqualTo("john");
        assertThat(dto.email).isEqualTo("john@example.com");
        assertThat(dto.role).isEqualTo("User");
        assertThat(dto.preferredLocale).isEqualTo("en");
        assertThat(dto.settings).containsEntry("theme", "dark");
    }

    @Test
    void toDto_user_handlesNullRole() {
        User u = new User();
        u.role = null;

        assertThat(mapper.toDto(u).role).isNull();
    }

    // -------------------------------------------------------------------------
    // Project
    // -------------------------------------------------------------------------

    @Test
    void toDto_project_returnsNull_forNullInput() {
        assertThat(mapper.toDto((Project) null)).isNull();
    }

    @Test
    void toDto_project_mapsAllFields() {
        User owner = new User();
        owner.id       = UUID.randomUUID();
        owner.name     = "Jane";
        owner.username = "jane";

        Project p = new Project("my-project");
        p.id          = UUID.randomUUID();
        p.description = "A great project";
        p.wordCount   = 42;
        p.owner       = owner;

        ProjectDto dto = mapper.toDto(p);

        assertThat(dto.id).isEqualTo(p.id);
        assertThat(dto.name).isEqualTo("my-project");
        assertThat(dto.description).isEqualTo("A great project");
        assertThat(dto.wordCount).isEqualTo(42);
        assertThat(dto.ownerId).isEqualTo(owner.id);
        assertThat(dto.ownerName).isEqualTo("Jane");
        assertThat(dto.ownerUsername).isEqualTo("jane");
    }

    @Test
    void toDto_project_handlesNullOwner() {
        Project p = new Project("proj");
        p.owner = null;

        ProjectDto dto = mapper.toDto(p);

        assertThat(dto.ownerId).isNull();
        assertThat(dto.ownerName).isNull();
    }

    // -------------------------------------------------------------------------
    // Key
    // -------------------------------------------------------------------------

    @Test
    void toDto_key_returnsNull_forNullInput() {
        assertThat(mapper.toDto((Key) null)).isNull();
    }

    @Test
    void toDto_key_mapsAllFields() {
        Project project = new Project("proj");
        project.id = UUID.randomUUID();

        Key k = new Key(project, "greeting");
        k.id        = UUID.randomUUID();
        k.wordCount = 3;

        KeyDto dto = mapper.toDto(k);

        assertThat(dto.id).isEqualTo(k.id);
        assertThat(dto.name).isEqualTo("greeting");
        assertThat(dto.wordCount).isEqualTo(3);
        assertThat(dto.projectId).isEqualTo(project.id);
        assertThat(dto.projectName).isEqualTo("proj");
    }

    // -------------------------------------------------------------------------
    // Locale
    // -------------------------------------------------------------------------

    @Test
    void toDto_locale_returnsNull_forNullInput() {
        assertThat(mapper.toDto((Locale) null)).isNull();
    }

    @Test
    void toDto_locale_mapsAllFields() {
        Project project = new Project("proj");
        project.id = UUID.randomUUID();

        Locale l = new Locale(project, "de");
        l.id        = UUID.randomUUID();
        l.wordCount = 10;

        LocaleDto dto = mapper.toDto(l);

        assertThat(dto.id).isEqualTo(l.id);
        assertThat(dto.name).isEqualTo("de");
        assertThat(dto.wordCount).isEqualTo(10);
        assertThat(dto.projectId).isEqualTo(project.id);
        assertThat(dto.projectName).isEqualTo("proj");
    }

    // -------------------------------------------------------------------------
    // Message
    // -------------------------------------------------------------------------

    @Test
    void toDto_message_returnsNull_forNullInput() {
        assertThat(mapper.toDto((Message) null)).isNull();
    }

    @Test
    void toDto_message_mapsAllFields() {
        Project project = new Project("proj");
        project.id = UUID.randomUUID();

        Locale locale = new Locale(project, "en");
        locale.id = UUID.randomUUID();

        Key key = new Key(project, "greeting");
        key.id = UUID.randomUUID();

        Message m = new Message(locale, key, "Hello");
        m.id        = UUID.randomUUID();
        m.wordCount = 1;

        MessageDto dto = mapper.toDto(m);

        assertThat(dto.id).isEqualTo(m.id);
        assertThat(dto.value).isEqualTo("Hello");
        assertThat(dto.wordCount).isEqualTo(1);
        assertThat(dto.localeId).isEqualTo(locale.id);
        assertThat(dto.localeName).isEqualTo("en");
        // localeDisplayName is stamped by MessageService (needs the viewer locale), not the mapper.
        assertThat(dto.localeDisplayName).isNull();
        assertThat(dto.keyId).isEqualTo(key.id);
        assertThat(dto.keyName).isEqualTo("greeting");
        assertThat(dto.projectId).isEqualTo(project.id);
        assertThat(dto.projectName).isEqualTo("proj");
    }

    // -------------------------------------------------------------------------
    // AccessToken
    // -------------------------------------------------------------------------

    @Test
    void toDto_accessToken_returnsNull_forNullInput() {
        assertThat(mapper.toDto((AccessToken) null)).isNull();
    }

    @Test
    void toDto_accessToken_mapsAllFields() {
        User user = new User();
        user.id       = UUID.randomUUID();
        user.username = "john";

        AccessToken t = new AccessToken();
        t.id    = 42L;
        t.name  = "my-token";
        t.key   = "abc123";
        t.scope = "read";
        t.user  = user;

        AccessTokenDto dto = mapper.toDto(t);

        assertThat(dto.id).isEqualTo(42L);
        assertThat(dto.name).isEqualTo("my-token");
        assertThat(dto.key).isEqualTo("abc123");
        assertThat(dto.scope).isEqualTo("read");
        assertThat(dto.userId).isEqualTo(user.id);
        assertThat(dto.userUsername).isEqualTo("john");
    }

    // -------------------------------------------------------------------------
    // Member (ProjectUser)
    // -------------------------------------------------------------------------

    @Test
    void toDto_member_returnsNull_forNullInput() {
        assertThat(mapper.toDto((ProjectUser) null)).isNull();
    }

    @Test
    void toDto_member_mapsAllFields() {
        Project project = new Project("proj");
        project.id = UUID.randomUUID();

        User user = new User();
        user.id       = UUID.randomUUID();
        user.username = "john";
        user.name     = "John";

        ProjectUser pu = new ProjectUser(ProjectRole.Manager);
        pu.id      = 7L;
        pu.project = project;
        pu.user    = user;

        MemberDto dto = mapper.toDto(pu);

        assertThat(dto.id).isEqualTo(7L);
        assertThat(dto.role).isEqualTo("Manager");
        assertThat(dto.projectId).isEqualTo(project.id);
        assertThat(dto.projectName).isEqualTo("proj");
        assertThat(dto.userId).isEqualTo(user.id);
        assertThat(dto.userUsername).isEqualTo("john");
        assertThat(dto.userName).isEqualTo("John");
    }
}


