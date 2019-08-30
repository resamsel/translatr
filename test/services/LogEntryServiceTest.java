package services;

import criterias.LogEntryCriteria;
import criterias.PagedListFactory;
import models.LogEntry;
import models.Project;
import models.User;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.LogEntryRepository;
import services.impl.CacheServiceImpl;
import services.impl.LogEntryServiceImpl;
import utils.CacheApiMock;
import utils.ProjectRepositoryMock;
import utils.UserRepositoryMock;

import javax.validation.Validator;
import java.util.UUID;

import static assertions.LogEntryAssert.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static utils.LogEntryRepositoryMock.createLogEntry;

public class LogEntryServiceTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(LogEntryServiceTest.class);

  private LogEntryRepository logEntryRepository;
  private LogEntryService logEntryService;
  private CacheService cacheService;
  private User johnSmith;
  private User janeDoe;
  private Project project1;

  @Test
  public void testById() {
    // mock logEntry
    LogEntry logEntry = createLogEntry(UUID.randomUUID(), johnSmith, project1);
    logEntryRepository.create(logEntry);

    // This invocation should feed the cache
    assertThat(cacheService.keys().keySet()).doesNotContain("activity:id:" + logEntry.id);
    assertThat(logEntryService.byId(logEntry.id)).userIsEqualTo(johnSmith);
    verify(logEntryRepository, times(1)).byId(eq(logEntry.id));

    // This invocation should use the cache, not the repository
    assertThat(cacheService.keys().keySet()).contains("activity:id:" + logEntry.id);
    assertThat(logEntryService.byId(logEntry.id)).userIsEqualTo(johnSmith);
    verify(logEntryRepository, times(1)).byId(eq(logEntry.id));

    // This should trigger cache invalidation
    logEntryService.update(createLogEntry(logEntry.id, janeDoe, project1));

    assertThat(cacheService.keys().keySet()).contains("activity:id:" + logEntry.id);
    assertThat(logEntryService.byId(logEntry.id)).userIsEqualTo(janeDoe);
    verify(logEntryRepository, times(1)).byId(eq(logEntry.id));
  }

  @Test
  public void testFindBy() {
    // mock logEntry
    LogEntry logEntry = createLogEntry(UUID.randomUUID(), johnSmith, project1);
    logEntryRepository.create(logEntry);

    // This invocation should feed the cache
    LogEntryCriteria criteria = new LogEntryCriteria().withUserId(logEntry.user.id);
    assertThat(logEntryService.findBy(criteria).getList().get(0))
        .as("uncached")
        .userIsEqualTo(johnSmith);
    verify(logEntryRepository, times(1)).findBy(eq(criteria));
    // This invocation should use the cache, not the repository
    assertThat(logEntryService.findBy(criteria).getList().get(0))
        .as("cached")
        .userIsEqualTo(johnSmith);
    verify(logEntryRepository, times(1)).findBy(eq(criteria));

    // This should trigger cache invalidation
    logEntryService.update(createLogEntry(logEntry.id, janeDoe, project1));

    assertThat(logEntryService.findBy(criteria).getList().get(0))
        .as("uncached (invalidated)")
        .userIsEqualTo(janeDoe);
    verify(logEntryRepository, times(2)).findBy(eq(criteria));
  }

  @Before
  public void before() {
    logEntryRepository = mock(LogEntryRepository.class,
        withSettings().invocationListeners(i -> LOGGER.debug("{}", i.getInvocation())));
    cacheService = new CacheServiceImpl(new CacheApiMock());
    logEntryService = new LogEntryServiceImpl(
        mock(Validator.class),
        cacheService,
        logEntryRepository
    );

    johnSmith = UserRepositoryMock.byUsername("johnsmith");
    janeDoe = UserRepositoryMock.byUsername("janedoe");
    project1 = ProjectRepositoryMock.byOwnerAndName(johnSmith.username, "project1");

    when(logEntryRepository.create(any())).then(this::persist);
    when(logEntryRepository.update(any())).then(this::persist);
  }

  private LogEntry persist(InvocationOnMock a) {
    LogEntry t = a.getArgument(0);
    when(logEntryRepository.byId(eq(t.id), any())).thenReturn(t);
    when(logEntryRepository.findBy(any())).thenReturn(PagedListFactory.create(t));
    return t;
  }
}
