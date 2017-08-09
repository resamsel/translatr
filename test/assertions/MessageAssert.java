package assertions;

import models.Message;

public class MessageAssert extends AbstractGenericAssert<MessageAssert, Message> {

  private MessageAssert(Message actual) {
    super("message", actual, MessageAssert.class);
  }

  public static MessageAssert assertThat(Message actual) {
    return new MessageAssert(actual);
  }

  public MessageAssert valueIsEqualTo(String expected) {
    return isEqualTo("value", expected, actual.value);
  }
}
