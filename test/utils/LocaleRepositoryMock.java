package utils;

import java.util.UUID;
import models.Locale;
import models.Project;

public class LocaleRepositoryMock {

  public static Locale createLocale(Locale locale, String name) {
    return createLocale(locale.id, locale.project.id, name);
  }

  public static Locale createLocale(UUID id, UUID projectId, String name) {
    Locale locale = new Locale();

    locale.id = id;
    locale.name = name;
    locale.project = new Project();
    locale.project.id = projectId;

    return locale;
  }
}
