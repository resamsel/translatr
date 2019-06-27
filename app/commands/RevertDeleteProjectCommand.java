package commands;

import dto.Project;
import mappers.KeyMapper;
import mappers.LocaleMapper;
import mappers.MessageMapper;
import mappers.ProjectMapper;
import models.Key;
import models.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.inject.Injector;
import play.mvc.Call;
import play.mvc.Http.Context;
import services.KeyService;
import services.LocaleService;
import services.MessageService;
import services.ProjectService;

import javax.inject.Inject;
import javax.persistence.Transient;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * @author resamsel
 * @version 19 Aug 2016
 */
public class RevertDeleteProjectCommand implements Command<models.Project> {

  private static final long serialVersionUID = -3106538601628220021L;

  private static final Logger LOGGER = LoggerFactory.getLogger(RevertDeleteProjectCommand.class);

  @Transient
  private final LocaleService localeService;
  @Transient
  private final KeyService keyService;
  private final MessageService messageService;

  private Project project;

  @Inject
  public RevertDeleteProjectCommand(LocaleService localeService, KeyService keyService,
      MessageService messageService) {
    this.localeService = localeService;
    this.keyService = keyService;
    this.messageService = messageService;
  }

  @Override
  public RevertDeleteProjectCommand with(models.Project project) {
    this.project = ProjectMapper.loadInto(project, localeService, keyService, messageService);
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
    projectService.update(model.withName(project.name).withDeleted(false));
    LOGGER.info("After save project: deleted = {}", model.deleted);

    Map<String, Key> keys = injector.instanceOf(KeyService.class)
        .save(project.keys.stream().map(k -> KeyMapper.toModel(k, model)).collect(Collectors.toList()))
        .stream().collect(toMap(k -> k.name, a -> a));
    Map<String, Locale> locales = injector.instanceOf(LocaleService.class)
        .save(project.locales.stream().map(l -> LocaleMapper.toModel(l, model)).collect(Collectors.toList()))
        .stream().collect(toMap(l -> l.name, a -> a));
    injector.instanceOf(MessageService.class).save(project.messages.stream()
        .map(m -> MessageMapper.toModel(m, locales.get(m.localeName), keys.get(m.keyName))).collect(toList()));
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
    return ProjectMapper.toModel(project).route();
  }
}
