package commands;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import controllers.routes;
import dto.Project;
import models.Key;
import models.Locale;
import play.inject.Injector;
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
  private static final long serialVersionUID = -3106538601628220021L;

  private static final Logger LOGGER = LoggerFactory.getLogger(RevertDeleteProjectCommand.class);

  private Project project;

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
  public void execute(Injector injector) {
    LOGGER.info("DTO Project #locales: {}, #keys: {}, #messages: {}", project.locales.size(),
        project.keys.size(), project.messages.size());

    ProjectService projectService = injector.instanceOf(ProjectService.class);

    models.Project model = projectService.byId(project.id);

    LOGGER.info("Before save project: deleted = {}", model.deleted);
    projectService.save(model.withName(project.name).withDeleted(false));
    LOGGER.info("After save project: deleted = {}", model.deleted);

    Map<String, Key> keys = injector.instanceOf(KeyService.class)
        .save(project.keys.stream().map(k -> k.toModel(model)).collect(Collectors.toList()))
        .stream().collect(toMap(k -> k.name, a -> a));
    Map<String, Locale> locales = injector.instanceOf(LocaleService.class)
        .save(project.locales.stream().map(l -> l.toModel(model)).collect(Collectors.toList()))
        .stream().collect(toMap(l -> l.name, a -> a));
    injector.instanceOf(MessageService.class).save(project.messages.stream()
        .map(m -> m.toModel(locales.get(m.localeName), keys.get(m.keyName))).collect(toList()));
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

  /**
   * @param project
   * @return
   */
  public static RevertDeleteProjectCommand from(models.Project project) {
    return new RevertDeleteProjectCommand().with(project);
  }
}
