package com.translatr.event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.translatr.model.ActionType;
import com.translatr.model.Project;
import com.translatr.model.User;
import com.translatr.dto.ProjectDto;
import io.vertx.mutiny.core.eventbus.EventBus;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.TransactionSynchronizationRegistry;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * The consumer re-loads the project/user by id in its own transaction, so the event must not
 * reach the bus before the publishing transaction commits — otherwise the log entry is written
 * with a null project (issue found while testing #364).
 */
class ActivityEventProducerTest {

    private final EventBus bus = mock(EventBus.class);
    private final TransactionSynchronizationRegistry txSync =
            mock(TransactionSynchronizationRegistry.class);
    private final ActivityEventProducer producer = new ActivityEventProducer(bus, txSync);

    @Test
    void insideTransaction_publishesOnlyAfterCommit() {
        when(txSync.getTransactionStatus()).thenReturn(Status.STATUS_ACTIVE);

        publishProjectCreate();

        verify(bus, never()).publish(any(), any());
        Synchronization sync = registeredSync();
        sync.beforeCompletion();
        verify(bus, never()).publish(any(), any());
        sync.afterCompletion(Status.STATUS_COMMITTED);
        verify(bus).publish(eq("activity"), any(ActivityEvent.class));
    }

    @Test
    void insideTransaction_rolledBack_neverPublishes() {
        when(txSync.getTransactionStatus()).thenReturn(Status.STATUS_ACTIVE);

        publishProjectCreate();
        registeredSync().afterCompletion(Status.STATUS_ROLLEDBACK);

        verify(bus, never()).publish(any(), any());
    }

    @Test
    void withoutTransaction_publishesImmediately() {
        when(txSync.getTransactionStatus()).thenReturn(Status.STATUS_NO_TRANSACTION);

        publishProjectCreate();

        verify(bus).publish(eq("activity"), any(ActivityEvent.class));
        verify(txSync, never()).registerInterposedSynchronization(any());
    }

    private void publishProjectCreate() {
        User user = new User();
        user.id = UUID.randomUUID();
        Project project = new Project();
        project.id = UUID.randomUUID();
        producer.publish(ActionType.Create, user, project, ProjectDto.class, null, new Object());
    }

    private Synchronization registeredSync() {
        ArgumentCaptor<Synchronization> captor = ArgumentCaptor.forClass(Synchronization.class);
        verify(txSync).registerInterposedSynchronization(captor.capture());
        return captor.getValue();
    }
}
