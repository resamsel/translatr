package com.translatr.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

    // Snapshots are DTOs that carry java.time fields (whenCreated/whenUpdated), so the
    // mapper needs the JSR-310 module; a bare ObjectMapper throws on Instant.
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public static <T> LogEntry from(ActionType type, User user, Project project,
                                    Class<T> clazz, T before, T after) {
        return from(type, user, project, clazz.getName(), before, after);
    }

    public static LogEntry from(ActionType type, User user, Project project,
                                String contentType, Object before, Object after) {
        LogEntry e = new LogEntry();
        e.type        = type;
        e.user        = user;
        e.project     = project;
        e.contentType = contentType;
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
