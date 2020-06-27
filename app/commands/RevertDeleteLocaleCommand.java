package commands;

import controllers.routes;
import criterias.KeyCriteria;
import dto.Locale;
import mappers.LocaleMapper;
import mappers.MessageMapper;
import models.Key;
import models.Project;
import play.inject.Injector;
import play.mvc.Call;
import play.mvc.Http.Context;
import services.KeyService;
import services.LocaleService;
import services.MessageService;
import services.ProjectService;

import javax.inject.Inject;
import java.util.Map;

import static controllers.AbstractController.*;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

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
    this.locale = LocaleMapper.loadInto(locale, messageService);
    return this;
  }

  @Override
  public void execute(Injector injector) {
    Project project = injector.instanceOf(ProjectService.class).byId(locale.projectId);

    models.Locale model = LocaleMapper.toModel(locale, project);
    injector.instanceOf(LocaleService.class).update(model);
    locale.id = model.id;

    Map<String, Key> keys =
        injector.instanceOf(KeyService.class).findBy(new KeyCriteria().withProjectId(project.id))
            .getList().stream().collect(toMap(k -> k.name, a -> a));
    injector.instanceOf(MessageService.class).save(locale.messages.values().stream()
        .map(m -> MessageMapper.toModel(m, model, keys.get(m.keyName))).collect(toList()));
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
