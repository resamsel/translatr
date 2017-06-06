package actors;

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

    public ChangeMessageWordCount(UUID messageId, UUID projectId, UUID localeId, UUID keyId,
        int wordCount, int wordCountDiff) {
      this.messageId = messageId;
      this.projectId = projectId;
      this.localeId = localeId;
      this.keyId = keyId;
      this.wordCount = wordCount;
      this.wordCountDiff = wordCountDiff;
    }
  }

  public static class ChangeWordCount {
    public final UUID id;
    public int wordCount;
    public int wordCountDiff;

    public ChangeWordCount(UUID id, int wordCount, int wordCountDiff) {
      this.id = id;
      this.wordCount = wordCount;
      this.wordCountDiff = wordCountDiff;
    }

    /**
     * @param b
     * @return
     */
    public ChangeWordCount merge(ChangeWordCount b) {
      wordCount += b.wordCount;
      wordCountDiff += b.wordCountDiff;
      return this;
    }
  }
}
