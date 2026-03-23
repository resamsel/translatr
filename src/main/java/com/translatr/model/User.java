package com.translatr.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "user_")
public class User extends PanacheEntityBase {

    public static final int USERNAME_LENGTH = 32;
    public static final int NAME_LENGTH     = 32;
    public static final int EMAIL_LENGTH    = 255;

    @Id
    @GeneratedValue
    public UUID id;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    public Instant whenCreated;

    @UpdateTimestamp
    @Column(nullable = false)
    public Instant whenUpdated;

    @Column(nullable = false)
    public boolean active = true;

    @NotBlank
    @Size(max = USERNAME_LENGTH)
    @Column(nullable = false, unique = true, length = USERNAME_LENGTH)
    public String username;

    @NotBlank
    @Size(max = NAME_LENGTH)
    @Column(nullable = false, length = NAME_LENGTH)
    public String name;

    @Column(length = EMAIL_LENGTH)
    public String email;

    public boolean emailValidated;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    public UserRole role = UserRole.User;

    @Column(length = 16)
    public String preferredLocale;

    @JsonIgnore
    @OneToMany(mappedBy = "owner")
    public List<Project> projects;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<LinkedAccount> linkedAccounts;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<AccessToken> accessTokens;

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    public List<ProjectUser> memberships;

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    public List<LogEntry> activities;

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    public List<UserFeatureFlag> features;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    public Map<String, String> settings = new HashMap<>();

    public boolean isAdmin() {
        return role == UserRole.Admin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User u)) return false;
        return Objects.equals(id, u.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return String.format("{\"username\": \"%s\"}", username);
    }
}
