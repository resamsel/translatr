package services;

import criterias.MessageCriteria;
import criterias.PagedListFactory;
import models.Message;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.MessageRepository;
import services.impl.CacheServiceImpl;
import services.impl.MessageServiceImpl;
import utils.CacheApiMock;

import javax.validation.Validator;
import java.util.UUID;

import static assertions.MessageAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static utils.MessageRepositoryMock.createMessage;

public class MessageServiceTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(MessageServiceTest.class);

  private MessageRepository messageRepository;
  private MessageService messageService;
  private CacheService cacheService;

  @Test
  public void testById() {
    // mock message
    Message message = createMessage(UUID.randomUUID(), UUID.randomUUID(), "value");
    messageRepository.create(message);

    // This invocation should feed the cache
    assertThat(cacheService.keys().keySet()).doesNotContain("message:id:" + message.id);
    assertThat(messageService.byId(message.id)).valueIsEqualTo("value");
    verify(messageRepository, times(1)).byId(eq(message.id));

    // This invocation should use the cache, not the repository
    assertThat(cacheService.keys().keySet()).contains("message:id:" + message.id);
    assertThat(messageService.byId(message.id)).valueIsEqualTo("value");
    verify(messageRepository, times(1)).byId(eq(message.id));

    // This should trigger cache invalidation
    messageService.update(createMessage(message, "value2"));

    assertThat(cacheService.keys().keySet()).contains("message:id:" + message.id);
    assertThat(messageService.byId(message.id)).valueIsEqualTo("value2");
    verify(messageRepository, times(1)).byId(eq(message.id));
  }

  @Test
  public void testFindBy() {
    // mock message
    Message message = createMessage(UUID.randomUUID(), UUID.randomUUID(), "value");
    messageRepository.create(message);

    // This invocation should feed the cache
    MessageCriteria criteria = new MessageCriteria().withProjectId(message.key.project.id);
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
    messageService.update(createMessage(message, "value3"));

    assertThat(messageService.findBy(criteria).getList().get(0))
        .as("uncached (invalidated)")
        .valueIsEqualTo("value3");
    verify(messageRepository, times(2)).findBy(eq(criteria));
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
        mock(LogEntryService.class),
        mock(AuthProvider.class)
    );

    when(messageRepository.create(any())).then(this::persist);
    when(messageRepository.update(any())).then(this::persist);
  }

  private Message persist(InvocationOnMock a) {
    Message t = a.getArgument(0);
    when(messageRepository.byId(eq(t.id), any())).thenReturn(t);
    when(messageRepository.findBy(any())).thenReturn(PagedListFactory.create(t));
    return t;
  }
}
