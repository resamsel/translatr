package com.translatr.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
public class LogEntry extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    public ActionType type;

    @Column(nullable = false, length = 64)
    public String contentType;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    public Instant whenCreated;

    @ManyToOne
    @JoinColumn(name = "user_id")
    public User user;

    @ManyToOne
    public Project project;

    @Column(length = 1024 * 1024)
    public String before;

    @Column(length = 1024 * 1024)
    public String after;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static <T> LogEntry from(ActionType type, User user, Project project,
                                    Class<T> clazz, T before, T after) {
        LogEntry e = new LogEntry();
        e.type        = type;
        e.user        = user;
        e.project     = project;
        e.contentType = clazz.getName();
        try {
            if (before != null) e.before = MAPPER.writeValueAsString(before);
            if (after  != null) e.after  = MAPPER.writeValueAsString(after);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return e;
    }

    public String getSimpleContentType() {
        return contentType == null ? null
                : contentType.replaceAll("^.*\\.", "").toLowerCase();
    }
}
