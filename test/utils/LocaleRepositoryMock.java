package utils;

import java.util.UUID;
import models.Locale;
import models.Project;

public class LocaleRepositoryMock {

  public static Locale createLocale(Locale locale, String name) {
    return createLocale(locale.id, locale.project.id, name);
  }

  public static Locale createLocale(UUID id, UUID projectId, String name) {
    Project project = new Project();
    project.id = projectId;
    return createLocale(id, project, name);
  }

  public static Locale createLocale(UUID id, Project project, String name) {
    Locale locale = new Locale();

    locale.id = id;
    locale.name = name;
    locale.project = project;

    return locale;
  }
}
