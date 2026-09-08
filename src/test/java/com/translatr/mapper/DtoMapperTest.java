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

        assertThat(dto.getId()).isEqualTo(u.id);
        assertThat(dto.getName()).isEqualTo("John");
        assertThat(dto.getUsername()).isEqualTo("john");
        assertThat(dto.getEmail()).isEqualTo("john@example.com");
        assertThat(dto.getRole()).isEqualTo("User");
        assertThat(dto.getPreferredLocale()).isEqualTo("en");
        assertThat(dto.getSettings()).containsEntry("theme", "dark");
    }

    @Test
    void toDto_user_handlesNullRole() {
        User u = new User();
        u.role = null;

        assertThat(mapper.toDto(u).getRole()).isNull();
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

        assertThat(dto.getId()).isEqualTo(p.id);
        assertThat(dto.getName()).isEqualTo("my-project");
        assertThat(dto.getDescription()).isEqualTo("A great project");
        assertThat(dto.getWordCount()).isEqualTo(42);
        assertThat(dto.getOwnerId()).isEqualTo(owner.id);
        assertThat(dto.getOwnerName()).isEqualTo("Jane");
        assertThat(dto.getOwnerUsername()).isEqualTo("jane");
    }

    @Test
    void toDto_project_handlesNullOwner() {
        Project p = new Project("proj");
        p.owner = null;

        ProjectDto dto = mapper.toDto(p);

        assertThat(dto.getOwnerId()).isNull();
        assertThat(dto.getOwnerName()).isNull();
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

        assertThat(dto.getId()).isEqualTo(k.id);
        assertThat(dto.getName()).isEqualTo("greeting");
        assertThat(dto.getWordCount()).isEqualTo(3);
        assertThat(dto.getProjectId()).isEqualTo(project.id);
        assertThat(dto.getProjectName()).isEqualTo("proj");
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

        assertThat(dto.getId()).isEqualTo(l.id);
        assertThat(dto.getName()).isEqualTo("de");
        assertThat(dto.getWordCount()).isEqualTo(10);
        assertThat(dto.getProjectId()).isEqualTo(project.id);
        assertThat(dto.getProjectName()).isEqualTo("proj");
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

        assertThat(dto.getId()).isEqualTo(m.id);
        assertThat(dto.getValue()).isEqualTo("Hello");
        assertThat(dto.getWordCount()).isEqualTo(1);
        assertThat(dto.getLocaleId()).isEqualTo(locale.id);
        assertThat(dto.getLocaleName()).isEqualTo("en");
        // localeDisplayName is stamped by MessageService (needs the viewer locale), not the mapper.
        assertThat(dto.getLocaleDisplayName()).isNull();
        assertThat(dto.getKeyId()).isEqualTo(key.id);
        assertThat(dto.getKeyName()).isEqualTo("greeting");
        assertThat(dto.getProjectId()).isEqualTo(project.id);
        assertThat(dto.getProjectName()).isEqualTo("proj");
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

        assertThat(dto.getId()).isEqualTo(42L);
        assertThat(dto.getName()).isEqualTo("my-token");
        assertThat(dto.getKey()).isEqualTo("abc123");
        assertThat(dto.getScope()).isEqualTo("read");
        assertThat(dto.getUserId()).isEqualTo(user.id);
        assertThat(dto.getUserUsername()).isEqualTo("john");
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

        assertThat(dto.getId()).isEqualTo(7L);
        assertThat(dto.getRole()).isEqualTo("Manager");
        assertThat(dto.getProjectId()).isEqualTo(project.id);
        assertThat(dto.getProjectName()).isEqualTo("proj");
        assertThat(dto.getUserId()).isEqualTo(user.id);
        assertThat(dto.getUserUsername()).isEqualTo("john");
        assertThat(dto.getUserName()).isEqualTo("John");
    }
}


