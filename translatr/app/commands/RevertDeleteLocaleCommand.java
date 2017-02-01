package commands;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;

import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import controllers.routes;
import criterias.KeyCriteria;
import dto.Locale;
import models.Key;
import models.Project;
import play.mvc.Call;
import play.mvc.Http.Context;
import services.LocaleService;
import services.MessageService;
import services.ProjectService;

public class RevertDeleteLocaleCommand implements Command<models.Locale> {
  private final ProjectService projectService;
  private final LocaleService localeService;
  private final MessageService messageService;

  private Locale locale;

  /**
   * 
   */
  @Inject
  public RevertDeleteLocaleCommand(ProjectService projectService, LocaleService localeService,
      MessageService messageService) {
    this.projectService = projectService;
    this.localeService = localeService;
    this.messageService = messageService;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RevertDeleteLocaleCommand with(models.Locale locale) {
    this.locale = Locale.from(locale).load();
    return this;
  }

  @Override
  public void execute() {
    Project project = projectService.byId(locale.projectId);

    models.Locale model = locale.toModel(project);
    localeService.save(model);
    locale.id = model.id;

    Map<String, Key> keys = Key.findBy(new KeyCriteria().withProjectId(project.id)).stream()
        .collect(groupingBy(k -> k.name, reducing(null, a -> a, (a, b) -> b)));
    messageService.save(locale.messages.stream().map(m -> m.toModel(model, keys.get(m.keyName)))
        .collect(Collectors.toList()));
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
    return routes.Projects.locales(locale.projectId);
  }
}
