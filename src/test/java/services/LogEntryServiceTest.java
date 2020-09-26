package services;

import actors.ActivityActorRef;
import criterias.GetCriteria;
import criterias.LogEntryCriteria;
import criterias.PagedListFactory;
import io.ebean.Ebean;
import io.ebean.PagedList;
import models.LogEntry;
import models.Project;
import models.User;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Http;
import repositories.LogEntryRepository;
import repositories.Persistence;
import services.impl.LogEntryServiceImpl;
import services.impl.NoCacheServiceImpl;
import utils.ProjectRepositoryMock;
import utils.UserRepositoryMock;

import javax.validation.Validator;
import java.util.UUID;

import static assertions.LogEntryAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static utils.LogEntryRepositoryMock.createLogEntry;

public class LogEntryServiceTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(LogEntryServiceTest.class);

  private LogEntryRepository logEntryRepository;
  private LogEntryService target;
  private CacheService cacheService;
  private Persistence persistence;
  private User johnSmith;
  private User janeDoe;
  private Project project1;

  @Before
  public void before() {
    logEntryRepository = mock(LogEntryRepository.class,
            withSettings().invocationListeners(i -> LOGGER.debug("{}", i.getInvocation())));
    cacheService = new NoCacheServiceImpl();
    persistence = mock(Persistence.class);
    target = new LogEntryServiceImpl(
            mock(Validator.class),
            cacheService,
            logEntryRepository,
            persistence,
            mock(AuthProvider.class),
            mock(ActivityActorRef.class)
    );

    johnSmith = UserRepositoryMock.byUsername("johnsmith");
    janeDoe = UserRepositoryMock.byUsername("janedoe");
    project1 = ProjectRepositoryMock.byOwnerAndName(johnSmith.username, "project1");

    when(persistence.createQuery(any())).then(invocation -> Ebean.createQuery(invocation.getArgument(0)));
  }

  @Test
  public void byId() {
    // given
    LogEntry logEntry = createLogEntry(UUID.randomUUID(), johnSmith, project1);
    Http.Request request = mock(Http.Request.class);

    when(logEntryRepository.byId(any(GetCriteria.class))).thenReturn(logEntry);

    // when
    LogEntry actual = target.byId(logEntry.id, request);

    // then
    assertThat(actual).userIsEqualTo(johnSmith);
  }

  @Test
  public void findBy() {
    // given
    LogEntry logEntry = createLogEntry(UUID.randomUUID(), johnSmith, project1);
    Http.Request request = mock(Http.Request.class);
    LogEntryCriteria criteria = LogEntryCriteria.from(request).withUserId(logEntry.user.id);

    when(logEntryRepository.findBy(eq(criteria))).thenReturn(PagedListFactory.create(logEntry));

    // when
    PagedList<LogEntry> actual = target.findBy(criteria);

    // then
    assertThat(actual.getList().get(0)).userIsEqualTo(johnSmith);
  }
}
