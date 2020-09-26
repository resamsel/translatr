package services;

import actors.ActivityActorRef;
import actors.MessageWordCountActorRef;
import criterias.GetCriteria;
import criterias.MessageCriteria;
import criterias.PagedListFactory;
import io.ebean.PagedList;
import mappers.MessageMapper;
import models.Message;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Http;
import repositories.MessageRepository;
import repositories.Persistence;
import services.impl.MessageServiceImpl;
import services.impl.NoCacheServiceImpl;

import javax.validation.Validator;
import java.util.UUID;

import static assertions.MessageAssert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static utils.MessageRepositoryMock.createMessage;

public class MessageServiceTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(MessageServiceTest.class);

  private MessageRepository messageRepository;
  private MessageService target;

  @Before
  public void before() {
    messageRepository = mock(MessageRepository.class,
            withSettings().invocationListeners(i -> LOGGER.debug("{}", i.getInvocation())));
    target = new MessageServiceImpl(
            mock(Validator.class),
            new NoCacheServiceImpl(),
            messageRepository,
            mock(LogEntryService.class),
            mock(AuthProvider.class),
            mock(MetricService.class),
            mock(ActivityActorRef.class),
            mock(MessageWordCountActorRef.class),
            mock(Persistence.class),
            mock(MessageMapper.class)
    );
  }

  @Test
  public void byId() {
    // given
    Message message = createMessage(UUID.randomUUID(), UUID.randomUUID(), "value");
    Http.Request request = mock(Http.Request.class);

    when(messageRepository.byId(any(GetCriteria.class))).thenReturn(message);

    // when
    Message actual = target.byId(message.id, request);

    // then
    assertThat(actual).valueIsEqualTo("value");
  }

  @Test
  public void findBy() {
    // given
    Message message = createMessage(UUID.randomUUID(), UUID.randomUUID(), "value");
    Http.Request request = mock(Http.Request.class);
    MessageCriteria criteria = MessageCriteria.from(request).withProjectId(message.key.project.id);

    when(messageRepository.findBy(eq(criteria))).thenReturn(PagedListFactory.create(message));

    // when
    PagedList<Message> actual = target.findBy(criteria);

    // then
    assertThat(actual.getList().get(0)).valueIsEqualTo("value");
  }
}
