package com.translatr.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
public class LinkedAccount extends PanacheEntityBase {

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

    @Column(nullable = false)
    public String providerUserId;

    @Column(nullable = false)
    public String providerKey;
}
