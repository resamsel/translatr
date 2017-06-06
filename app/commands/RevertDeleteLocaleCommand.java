package commands;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.Map;

import controllers.Locales;
import controllers.routes;
import criterias.KeyCriteria;
import dto.Locale;
import models.Key;
import models.Project;
import play.inject.Injector;
import play.mvc.Call;
import play.mvc.Http.Context;
import services.KeyService;
import services.LocaleService;
import services.MessageService;
import services.ProjectService;

public class RevertDeleteLocaleCommand implements Command<models.Locale> {
  private static final long serialVersionUID = -1628328599712791510L;

  private Locale locale;

  /**
   * {@inheritDoc}
   */
  @Override
  public RevertDeleteLocaleCommand with(models.Locale locale) {
    this.locale = Locale.from(locale).load();
    return this;
  }

  @Override
  public void execute(Injector injector) {
    Project project = injector.instanceOf(ProjectService.class).byId(locale.projectId);

    models.Locale model = locale.toModel(project);
    injector.instanceOf(LocaleService.class).save(model);
    locale.id = model.id;

    Map<String, Key> keys =
        injector.instanceOf(KeyService.class).findBy(new KeyCriteria().withProjectId(project.id))
            .getList().stream().collect(toMap(k -> k.name, a -> a));
    injector.instanceOf(MessageService.class).save(locale.messages.values().stream()
        .map(m -> m.toModel(model, keys.get(m.keyName))).collect(toList()));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getMessage() {
    return Context.current().messages().at("locale.deleted", locale.name);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Call redirect() {
    return routes.Projects.locales(locale.projectId, Locales.DEFAULT_SEARCH, Locales.DEFAULT_ORDER,
        Locales.DEFAULT_LIMIT, Locales.DEFAULT_OFFSET);
  }

  /**
   * @param locale
   * @return
   */
  public static RevertDeleteLocaleCommand from(models.Locale locale) {
    return new RevertDeleteLocaleCommand().with(locale);
  }
}
