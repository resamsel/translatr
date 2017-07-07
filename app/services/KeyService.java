package services;

import com.google.inject.ImplementedBy;
import criterias.KeyCriteria;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import models.Key;
import services.impl.KeyServiceImpl;

/**
 *
 * @author resamsel
 * @version 29 Aug 2016
 */
@ImplementedBy(KeyServiceImpl.class)
public interface KeyService extends ModelService<Key, UUID, KeyCriteria> {
  /**
   * @param keyIds
   * @param localesSize
   * @return
   */
  Map<UUID, Double> progress(List<UUID> keyIds, long localesSize);

  /**
   * @param keyId
   * @param wordCountDiff
   */
  void increaseWordCountBy(UUID keyId, int wordCountDiff);

  /**
   * @param projectId
   */
  void resetWordCount(UUID projectId);
}
