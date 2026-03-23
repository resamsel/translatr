package com.translatr.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
public class AccessToken extends PanacheEntityBase {

    public static final int NAME_LENGTH  = 32;
    public static final int KEY_LENGTH   = 64;

    @Id
    @GeneratedValue
    @Column(name = "id")
    public Long id;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    public Instant whenCreated;

    @UpdateTimestamp
    @Column(nullable = false)
    public Instant whenUpdated;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    public User user;

    @Size(max = NAME_LENGTH)
    @Column(nullable = false, length = NAME_LENGTH)
    public String name;

    @Size(max = KEY_LENGTH)
    @Column(nullable = false, unique = true, length = KEY_LENGTH)
    public String key;

    @Column(length = 255)
    public String scope;
}
