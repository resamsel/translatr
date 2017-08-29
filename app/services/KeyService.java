package services;

import com.google.inject.ImplementedBy;
import criterias.KeyCriteria;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import models.Key;
import models.Project;
import services.impl.KeyServiceImpl;

/**
 *
 * @author resamsel
 * @version 29 Aug 2016
 */
@ImplementedBy(KeyServiceImpl.class)
public interface KeyService extends ModelService<Key, UUID, KeyCriteria> {
  Map<UUID, Double> progress(List<UUID> keyIds, long localesSize);

  void increaseWordCountBy(UUID keyId, int wordCountDiff);

  void resetWordCount(UUID projectId);

  List<Key> latest(Project project, int limit);

  Key byProjectAndName(Project project, String keyName);
}
