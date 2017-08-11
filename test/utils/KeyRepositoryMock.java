package utils;

import java.util.UUID;
import models.Key;
import models.Locale;
import models.Project;

public class KeyRepositoryMock {

  public static Key createKey(Key key, String name) {
    return createKey(key.id, key.project.id, name);
  }

  public static Key createKey(UUID id, UUID projectId, String name) {
    Key m = new Key();

    m.id = id;
    m.name = name;
    m.project = new Project();
    m.project.id = projectId;

    return m;
  }
}
