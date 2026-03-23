package com.translatr.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"locale_id", "key_id"}))
public class Message extends PanacheEntityBase {

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
    public Locale locale;

    @NotNull
    @ManyToOne(optional = false)
    public Key key;

    @Column(nullable = false, length = 1024 * 1024)
    public String value;

    public Integer wordCount;

    public Message() {}

    public Message(Locale locale, Key key, String value) {
        this.locale = locale;
        this.key    = key;
        this.value  = value;
    }
}
