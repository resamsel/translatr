package com.translatr.event;

import static org.assertj.core.api.Assertions.assertThat;

import com.translatr.dto.MemberDto;
import com.translatr.dto.ProjectDto;
import com.translatr.model.ActionType;
import com.translatr.model.LogEntry;
import com.translatr.model.User;
import com.translatr.repository.LogEntryRepository;
import com.translatr.repository.UserRepository;
import com.translatr.service.ActivityService;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ActivityEventConsumerTest {

    @Inject ActivityEventProducer producer;
    @Inject LogEntryRepository    logRepo;
    @Inject UserRepository        userRepo;
    @Inject ActivityService       activityService;

    @Test
    void publishingActivity_persistsALogEntry() throws InterruptedException {
        UUID userId = createUser();
        User user = QuarkusTransaction.requiringNew().call(() -> userRepo.findById(userId));

        ProjectDto after = new ProjectDto();
        after.name        = "brand-new-project";
        after.whenCreated = java.time.Instant.EPOCH; // DTOs carry java.time fields — must serialize

        producer.publish(ActionType.Create, user, null, ProjectDto.class, null, after);

        LogEntry entry = awaitLatestEntryForUser(userId);

        assertThat(entry).as("a log_entry row should have been persisted").isNotNull();
        assertThat(entry.type).isEqualTo(ActionType.Create);
        // Wire contract the Angular activity list expects: "dto.<SimpleName>", not the FQCN.
        assertThat(entry.contentType).isEqualTo("dto.Project");
        assertThat(entry.before).isNull();
        assertThat(entry.after).contains("brand-new-project");
        assertThat(entry.user).isNotNull();
        assertThat(entry.user.id).isEqualTo(userId);
    }

    @Test
    void memberActivity_usesLegacyProjectUserContentType() throws InterruptedException {
        UUID userId = createUser();
        User user = QuarkusTransaction.requiringNew().call(() -> userRepo.findById(userId));

        MemberDto after = new MemberDto();
        after.role = "Translator";

        producer.publish(ActionType.Create, user, null, MemberDto.class, null, after);

        LogEntry entry = awaitLatestEntryForUser(userId);
        assertThat(entry).isNotNull();
        assertThat(entry.contentType).isEqualTo("dto.ProjectUser");
    }

    @Test
    void getAggregates_returnsDailyCountsForTheUser() throws InterruptedException {
        UUID userId = createUser();
        User user = QuarkusTransaction.requiringNew().call(() -> userRepo.findById(userId));

        ProjectDto after = new ProjectDto();
        after.name = "aggregated-project";
        producer.publish(ActionType.Create, user, null, ProjectDto.class, null, after);
        awaitLatestEntryForUser(userId);

        var aggregates = activityService.getAggregates(null, userId, 0, 100);

        assertThat(aggregates.list).isNotEmpty();
        assertThat(aggregates.list.get(0).date).isNotNull();
        assertThat(aggregates.list.get(0).value).isGreaterThanOrEqualTo(1);
    }

    private UUID createUser() {
        return QuarkusTransaction.requiringNew().call(() -> {
            User u = new User();
            u.username = "activity-test-" + System.nanoTime();
            u.name     = "Activity Test";
            userRepo.persist(u);
            return u.id;
        });
    }

    private LogEntry awaitLatestEntryForUser(UUID userId) throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            LogEntry entry = QuarkusTransaction.requiringNew().call(() ->
                    logRepo.find("user.id = ?1 ORDER BY whenCreated DESC", userId).firstResult());
            if (entry != null) {
                return entry;
            }
            Thread.sleep(100);
        }
        return null;
    }
}
