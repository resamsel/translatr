package repositories;

import com.google.inject.ImplementedBy;
import criterias.KeyCriteria;
import java.util.List;
import java.util.UUID;
import models.Key;
import models.Project;
import repositories.impl.KeyRepositoryImpl;

@ImplementedBy(KeyRepositoryImpl.class)
public interface KeyRepository extends ModelRepository<Key, UUID, KeyCriteria> {

  List<Key> latest(Project project, int limit);

  Key byProjectAndName(Project project, String name);
}
