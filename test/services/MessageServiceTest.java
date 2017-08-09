package services;

import static assertions.MessageAssert.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import criterias.HasNextPagedList;
import criterias.MessageCriteria;
import java.util.UUID;
import javax.validation.Validator;
import models.Key;
import models.Message;
import models.Project;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.MessageRepository;
import services.impl.CacheServiceImpl;
import services.impl.MessageServiceImpl;
import utils.CacheApiMock;

public class MessageServiceTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(MessageServiceTest.class);

  private MessageRepository messageRepository;
  private MessageServiceImpl messageService;
  private CacheServiceImpl cacheService;

  @Test
  public void testById() {
    // mock message
    Message message = createMessage(UUID.randomUUID(), UUID.randomUUID(), "value");
    when(messageRepository.byId(eq(message.id))).thenReturn(message);
    when(messageRepository.update(any())).thenAnswer(a -> a.getArguments()[0]);

    // This invocation should feed the cache
    assertThat(cacheService.keys().keySet()).doesNotContain("message:id:" + message.id);
    assertThat(messageService.byId(message.id)).valueIsEqualTo("value");
    verify(messageRepository, times(1)).byId(eq(message.id));

    // This invocation should use the cache, not the repository
    assertThat(cacheService.keys().keySet()).contains("message:id:" + message.id);
    assertThat(messageService.byId(message.id)).valueIsEqualTo("value");
    verify(messageRepository, times(1)).byId(eq(message.id));

    // This should trigger cache invalidation
    message = createMessage(message, "value2");
    messageService.update(message);

    when(messageRepository.byId(eq(message.id))).thenReturn(message);
    assertThat(cacheService.keys().keySet()).doesNotContain("message:id:" + message.id);
    assertThat(messageService.byId(message.id)).valueIsEqualTo("value2");
    verify(messageRepository, times(2)).byId(eq(message.id));
  }

  @Test
  public void testFindBy() {
    // mock message
    Message message = createMessage(UUID.randomUUID(), UUID.randomUUID(), "value");
    MessageCriteria criteria = new MessageCriteria().withProjectId(message.key.project.id);
    when(messageRepository.findBy(eq(criteria))).thenReturn(HasNextPagedList.create(message));
    when(messageRepository.update(any())).thenAnswer(a -> a.getArguments()[0]);

    // This invocation should feed the cache
    assertThat(messageService.findBy(criteria).getList().get(0))
        .as("uncached")
        .valueIsEqualTo("value");
    verify(messageRepository, times(1)).findBy(eq(criteria));
    // This invocation should use the cache, not the repository
    assertThat(messageService.findBy(criteria).getList().get(0))
        .as("cached")
        .valueIsEqualTo("value");
    verify(messageRepository, times(1)).findBy(eq(criteria));

    // This should trigger cache invalidation
    message = createMessage(message, "value2");
    messageService.update(message);

    when(messageRepository.findBy(eq(criteria))).thenReturn(HasNextPagedList.create(message));
    assertThat(messageService.findBy(criteria).getList().get(0))
        .as("uncached (invalidated)")
        .valueIsEqualTo("value2");
    verify(messageRepository, times(2)).findBy(eq(criteria));
  }

  private Message createMessage(Message message, String value) {
    return createMessage(message.id, message.key.project.id, value);
  }

  private Message createMessage(UUID id, UUID projectId, String value) {
    Message message = new Message();

    message.id = id;
    message.value = value;
    message.key = new Key();
    message.key.project = new Project();
    message.key.project.id = projectId;

    LOGGER.debug("Creating message with ID {} and project ID {}", message.id, message.key.project.id);

    return message;
  }

  @Before
  public void before() {
    messageRepository = mock(MessageRepository.class,
        withSettings().invocationListeners(i -> LOGGER.debug("{}", i.getInvocation())));
    cacheService = new CacheServiceImpl(new CacheApiMock());
    messageService = new MessageServiceImpl(
        mock(Validator.class),
        cacheService,
        messageRepository,
        mock(LogEntryService.class)
    );
  }
}
