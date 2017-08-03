package commands;

import static controllers.AbstractController.DEFAULT_LIMIT;
import static controllers.AbstractController.DEFAULT_OFFSET;
import static controllers.AbstractController.DEFAULT_ORDER;
import static controllers.AbstractController.DEFAULT_SEARCH;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import controllers.routes;
import criterias.KeyCriteria;
import dto.Locale;
import java.util.Map;
import javax.inject.Inject;
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

  private final MessageService messageService;

  private Locale locale;

  @Inject
  public RevertDeleteLocaleCommand(MessageService messageService) {
    this.messageService = messageService;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RevertDeleteLocaleCommand with(models.Locale locale) {
    this.locale = Locale.from(locale).load(messageService);
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
    return routes.Projects.localesBy(locale.projectOwnerUsername, locale.projectName,
        DEFAULT_SEARCH, DEFAULT_ORDER, DEFAULT_LIMIT, DEFAULT_OFFSET);
  }
}
