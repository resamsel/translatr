package utils;

import java.util.UUID;
import models.Key;
import models.Message;
import models.Project;

public class MessageRepositoryMock {

  public static Message createMessage(Message message, String value) {
    return createMessage(message.id, message.key.project.id, value);
  }

  public static Message createMessage(UUID id, UUID projectId, String value) {
    Message message = new Message();

    message.id = id;
    message.value = value;
    message.key = new Key();
    message.key.project = new Project();
    message.key.project.id = projectId;

    return message;
  }
}
