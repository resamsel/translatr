package actors;

import play.mvc.Http;

import java.util.UUID;

/**
 * @author resamsel
 * @version 6 Jun 2017
 */
public class WordCountProtocol {

  public static class ChangeMessageWordCount {

    public final UUID messageId;
    public final UUID projectId;
    public final UUID localeId;
    public final UUID keyId;
    public final int wordCount;
    public final int wordCountDiff;
    public final Http.Request request;

    public ChangeMessageWordCount(UUID messageId, UUID projectId, UUID localeId, UUID keyId,
                                  int wordCount, int wordCountDiff, Http.Request request) {
      this.messageId = messageId;
      this.projectId = projectId;
      this.localeId = localeId;
      this.keyId = keyId;
      this.wordCount = wordCount;
      this.wordCountDiff = wordCountDiff;
      this.request = request;
    }
  }

  public static class ChangeWordCount {

    public final UUID id;
    public int wordCount;
    public int wordCountDiff;
    public final Http.Request request;

    public ChangeWordCount(UUID id, int wordCount, int wordCountDiff, Http.Request request) {
      this.id = id;
      this.wordCount = wordCount;
      this.wordCountDiff = wordCountDiff;
      this.request = request;
    }

    public static ChangeWordCount from(ChangeMessageWordCount wordCount, UUID id) {
      return new ChangeWordCount(id, wordCount.wordCount, wordCount.wordCountDiff, wordCount.request);
    }

    public ChangeWordCount merge(ChangeWordCount b) {
      wordCount += b.wordCount;
      wordCountDiff += b.wordCountDiff;
      return this;
    }
  }
}
