package com.translatr.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "name"}))
public class Locale extends PanacheEntityBase {

    public static final int NAME_LENGTH = 15;

    @Id
    @GeneratedValue
    public UUID id;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    public Instant whenCreated;

    @UpdateTimestamp
    @Column(nullable = false)
    public Instant whenUpdated;

    @NotNull
    @ManyToOne(optional = false)
    public Project project;

    @NotBlank
    @Size(max = NAME_LENGTH)
    @Column(nullable = false, length = NAME_LENGTH)
    public String name;

    public Integer wordCount;

    @JsonIgnore
    @OneToMany(mappedBy = "locale")
    public List<Message> messages;

    public Locale() {}

    public Locale(Project project, String name) {
        this.project = project;
        this.name = name;
    }
}
