package utils;

import java.util.UUID;
import models.Key;
import models.Project;

public class KeyRepositoryMock {

  public static Key createKey(Key key, String name) {
    return createKey(key.id, key.project, name);
  }

  public static Key createKey(UUID id, UUID projectId, String name) {
    Project project = new Project();

    project.id = projectId;

    return createKey(id, project, name);
  }

  public static Key createKey(UUID id, Project project, String name) {
    Key m = new Key();

    m.id = id;
    m.name = name;
    m.project = project;

    return m;
  }
}
