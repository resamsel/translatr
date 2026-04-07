package com.translatr.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "user_id"}))
public class ProjectUser extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "project_user_seq_gen")
    @SequenceGenerator(name = "project_user_seq_gen", sequenceName = "project_user_id_seq", allocationSize = 1)
    @Column(name = "id")
    public Long id;

    @JsonIgnore
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    public Instant whenCreated;

    @JsonIgnore
    @UpdateTimestamp
    @Column(nullable = false)
    public Instant whenUpdated;

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    public Project project;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    public User user;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    public ProjectRole role;

    public ProjectUser() {}

    public ProjectUser(ProjectRole role) { this.role = role; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProjectUser pu)) return false;
        return Objects.equals(project, pu.project) && Objects.equals(user, pu.user);
    }

    @Override
    public int hashCode() { return Objects.hash(project, user); }
}
