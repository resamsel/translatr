package com.translatr.service;

import com.translatr.model.Key;
import com.translatr.model.Locale;
import com.translatr.model.Message;
import com.translatr.model.Project;
import com.translatr.model.User;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@QuarkusTest
class ProgressServiceTest {

    @Inject ProgressService progress;

    private UUID projectId;
    private UUID deId;
    private UUID frId;
    private UUID k1Id;
    private UUID k2Id;
    private UUID k3Id;

    /**
     * A project with 2 locales (de, fr) and 3 keys (k1, k2, k3). Translations:
     * de -> k1, k2   (2 of 3 keys)
     * fr -> k1       (1 of 3 keys)
     * So k1 is fully translated (2/2 locales), k2 half (1/2), k3 not at all (0/2).
     * Project total: 3 messages of a possible 2 locales x 3 keys = 6.
     *
     * <p>Called from inside each {@code @TestTransaction} test so the rows share the test's
     * transaction and are rolled back with it.
     */
    private void seed() {
        User owner = new User();
        owner.username = "progress-owner-" + System.nanoTime();
        owner.name     = "Progress Owner";
        owner.persist();

        Project project = new Project("progress-project-" + System.nanoTime());
        project.owner = owner;
        project.persist();
        projectId = project.id;

        Locale de = new Locale(project, "de");
        Locale fr = new Locale(project, "fr");
        de.persist();
        fr.persist();
        deId = de.id;
        frId = fr.id;

        Key k1 = new Key(project, "k1");
        Key k2 = new Key(project, "k2");
        Key k3 = new Key(project, "k3");
        k1.persist();
        k2.persist();
        k3.persist();
        k1Id = k1.id;
        k2Id = k2.id;
        k3Id = k3.id;

        new Message(de, k1, "de-k1").persist();
        new Message(de, k2, "de-k2").persist();
        new Message(fr, k1, "fr-k1").persist();
    }

    @Test
    @TestTransaction
    void localeProgress_isMessagesOverProjectKeyCount() {
        seed();

        Map<UUID, Double> byLocale = progress.localeProgress(projectId);

        assertThat(byLocale.get(deId)).isCloseTo(2.0 / 3.0, within(1e-9));
        assertThat(byLocale.get(frId)).isCloseTo(1.0 / 3.0, within(1e-9));
    }

    @Test
    @TestTransaction
    void keyProgress_isMessagesOverProjectLocaleCount() {
        seed();

        Map<UUID, Double> byKey = progress.keyProgress(projectId);

        assertThat(byKey.get(k1Id)).isCloseTo(1.0, within(1e-9));
        assertThat(byKey.get(k2Id)).isCloseTo(0.5, within(1e-9));
        // k3 has no messages - callers treat a missing entry as 0.0
        assertThat(byKey.getOrDefault(k3Id, 0.0)).isEqualTo(0.0);
    }

    @Test
    @TestTransaction
    void projectProgress_isMessagesOverLocalesTimesKeys() {
        seed();

        Map<UUID, Double> byProject = progress.projectProgress(List.of(projectId));

        assertThat(byProject.get(projectId)).isCloseTo(3.0 / 6.0, within(1e-9));
    }

    @Test
    @TestTransaction
    void projectProgress_returnsEmptyMap_forEmptyInput() {
        assertThat(progress.projectProgress(List.of())).isEmpty();
    }
}
