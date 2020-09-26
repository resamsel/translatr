package services;

import actors.ActivityActorRef;
import criterias.GetCriteria;
import criterias.KeyCriteria;
import criterias.PagedListFactory;
import io.ebean.PagedList;
import mappers.KeyMapper;
import models.Key;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Http;
import repositories.KeyRepository;
import repositories.MessageRepository;
import repositories.Persistence;
import services.impl.KeyServiceImpl;
import services.impl.NoCacheServiceImpl;
import utils.ProjectRepositoryMock;

import javax.validation.Validator;
import java.util.UUID;

import static assertions.KeyAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static utils.KeyRepositoryMock.createKey;

public class KeyServiceTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(KeyServiceTest.class);

  private KeyRepository keyRepository;
  private KeyService target;

  @Before
  public void before() {
    keyRepository = mock(KeyRepository.class,
            withSettings().invocationListeners(i -> LOGGER.debug("{}", i.getInvocation())));
    target = new KeyServiceImpl(
            mock(Validator.class),
            new NoCacheServiceImpl(),
            keyRepository,
            mock(LogEntryService.class),
            mock(Persistence.class),
            mock(AuthProvider.class),
            mock(MetricService.class),
            mock(ActivityActorRef.class),
            mock(KeyMapper.class),
            mock(PermissionService.class),
            mock(MessageRepository.class)
    );
  }

  @Test
  public void byId() {
    // given
    Key key = createKey(UUID.randomUUID(), UUID.randomUUID(), "ab");
    Http.Request request = mock(Http.Request.class);

    when(keyRepository.byId(any(GetCriteria.class))).thenReturn(key);

    // when
    Key actual = target.byId(key.id, request);

    // then
    assertThat(actual).nameIsEqualTo("ab");
  }

  @Test
  public void findBy() {
    // given
    Key key = createKey(UUID.randomUUID(), UUID.randomUUID(), "a");
    Http.Request request = mock(Http.Request.class);
    KeyCriteria criteria = KeyCriteria.from(request).withProjectId(key.project.id);

    when(keyRepository.findBy(eq(criteria))).thenReturn(PagedListFactory.create(key));

    // when
    PagedList<Key> actual = target.findBy(criteria);

    // then
    assertThat(actual.getList().get(0)).nameIsEqualTo("a");
  }

  @Test
  public void byProjectAndName() {
    // given
    Key key = createKey(UUID.randomUUID(),
            ProjectRepositoryMock.byOwnerAndName("johnsmith", "project1"), "a");
    Http.Request request = mock(Http.Request.class);

    when(keyRepository.byProjectAndName(eq(key.project), eq(key.name))).thenReturn(key);

    // when
    Key actual = target.byProjectAndName(key.project, key.name, request);

    // then
    assertThat(actual).nameIsEqualTo("a");
  }
}
