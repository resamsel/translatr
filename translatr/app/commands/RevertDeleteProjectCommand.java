package commands;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;

import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import controllers.routes;
import dto.Project;
import models.Key;
import models.Locale;
import play.mvc.Call;
import play.mvc.Http.Context;
import services.KeyService;
import services.LocaleService;
import services.MessageService;
import services.ProjectService;

/**
 *
 * @author resamsel
 * @version 19 Aug 2016
 */
public class RevertDeleteProjectCommand implements Command<models.Project> {
  private static final Logger LOGGER = LoggerFactory.getLogger(RevertDeleteProjectCommand.class);

  private Project project;

  private final ProjectService projectService;

  private final LocaleService localeService;

  private final KeyService keyService;

  private final MessageService messageService;

  /**
   * @param project
   */
  @Inject
  public RevertDeleteProjectCommand(ProjectService projectService, LocaleService localeService,
      KeyService keyService, MessageService messageService) {
    this.projectService = projectService;
    this.localeService = localeService;
    this.keyService = keyService;
    this.messageService = messageService;
  }

  /**
   * @param project
   * @return
   */
  @Override
  public RevertDeleteProjectCommand with(models.Project project) {
    this.project = Project.from(project).load();
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void execute() {
    LOGGER.info("DTO Project #locales: {}, #keys: {}, #messages: {}", project.locales.size(),
        project.keys.size(), project.messages.size());

    models.Project model = projectService.byId(project.id);

    LOGGER.info("Before save project: deleted = {}", model.deleted);
    projectService.save(model.withName(project.name).withDeleted(false));
    LOGGER.info("After save project: deleted = {}", model.deleted);

    Map<String, Key> keys = keyService
        .save(project.keys.stream().map(k -> k.toModel(model)).collect(Collectors.toList()))
        .stream().collect(groupingBy(k -> k.name, reducing(null, a -> a, (a, b) -> b)));
    Map<String, Locale> locales = localeService
        .save(project.locales.stream().map(l -> l.toModel(model)).collect(Collectors.toList()))
        .stream().collect(groupingBy(l -> l.name, reducing(null, a -> a, (a, b) -> b)));
    messageService.save(project.messages.stream()
        .map(m -> m.toModel(locales.get(m.localeName), keys.get(m.keyName)))
        .collect(Collectors.toList()));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getMessage() {
    return Context.current().messages().at("project.deleted", project.name);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Call redirect() {
    return routes.Projects.project(project.id);
  }
}
