package com.translatr.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"owner_id", "name"}))
public class Project extends PanacheEntityBase {

    public static final int NAME_LENGTH = 255;

    @Id
    @GeneratedValue
    public UUID id;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    public Instant whenCreated;

    @UpdateTimestamp
    @Column(nullable = false)
    public Instant whenUpdated;

    public boolean deleted;

    @ManyToOne(optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    public User owner;

    @NotBlank
    @Size(max = NAME_LENGTH)
    @Column(nullable = false, length = NAME_LENGTH)
    public String name;

    @Size(max = 2000)
    @Column(length = 2000)
    public String description;

    public Integer wordCount;

    @JsonIgnore
    @OneToMany(mappedBy = "project")
    public List<Locale> locales;

    @JsonIgnore
    @OneToMany(mappedBy = "project")
    public List<Key> keys;

    @JsonIgnore
    @OneToMany(mappedBy = "project")
    public List<ProjectUser> members;

    public Project() {}

    public Project(String name) { this.name = name; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Project p)) return false;
        return Objects.equals(id, p.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return String.format("{\"name\": \"%s\", \"owner\": %s}", name, owner);
    }
}
