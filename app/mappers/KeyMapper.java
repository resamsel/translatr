package mappers;

import dto.Key;
import dto.Message;
import models.Project;
import models.User;

import static java.util.stream.Collectors.toMap;

public class KeyMapper {
  public static models.Key toModel(Key in) {
    return toModel(in, ProjectMapper.toModel(in));
  }

  public static models.Key toModel(Key in, Project project) {
    models.Key out = new models.Key();

    out.id = in.id;
    out.whenCreated = in.whenCreated;
    out.whenUpdated = in.whenUpdated;
    out.project = project;
    out.name = in.name;

    return out;
  }

  public static models.Key toModel(Message in) {
    if (in == null) {
      return null;
    }

    models.Key out = new models.Key();

    out.id = in.keyId;
    out.name = in.keyName;

    if (in.projectId != null) {
      out.project = new Project()
          .withId(in.projectId)
          .withName(in.projectName);

      if (in.projectOwnerUsername != null) {
        out.project.owner = new User().withUsername(in.projectOwnerUsername);
      }
    }

    return out;
  }

  public static Key toDto(models.Key in) {
    Key out = new Key();

    out.id = in.id;
    out.whenCreated = in.whenCreated;
    out.whenUpdated = in.whenUpdated;
    out.projectId = in.project.id;
    out.projectName = in.project.name;
    out.projectOwnerUsername = in.project.owner.username;
    out.name = in.name;
    out.pathName = in.getPathName();
    out.progress = in.progress;

    if (in.messages != null && !in.messages.isEmpty()) {
      out.messages =
          in.messages.stream()
              .map(MessageMapper::toDto)
              .filter(m -> m.localeName != null)
              .collect(toMap(m -> m.localeName, m -> m));
    }

    return out;
  }
}
