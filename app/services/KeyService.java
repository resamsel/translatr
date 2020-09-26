package services;

import com.google.inject.ImplementedBy;
import criterias.KeyCriteria;
import models.Key;
import models.Project;
import play.mvc.Http;
import services.impl.KeyServiceImpl;

import java.util.UUID;

/**
 * @author resamsel
 * @version 29 Aug 2016
 */
@ImplementedBy(KeyServiceImpl.class)
public interface KeyService extends ModelService<Key, UUID, KeyCriteria> {
  void increaseWordCountBy(UUID keyId, int wordCountDiff, Http.Request request);

  void resetWordCount(UUID projectId);

  Key byProjectAndName(Project project, String keyName, Http.Request request);

  Key byOwnerAndProjectAndName(String username, String projectName, String keyName, Http.Request request, String... fetches);
}
