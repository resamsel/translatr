package commands;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import controllers.Keys;
import controllers.routes;
import criterias.LocaleCriteria;
import dto.Key;
import dto.Message;
import models.Locale;
import models.Project;
import play.mvc.Call;
import play.mvc.Http.Context;
import services.KeyService;
import services.LocaleService;
import services.MessageService;
import services.ProjectService;

public class RevertDeleteKeyCommand implements Command<models.Key> {
  private final ProjectService projectService;
  private final LocaleService localeService;
  private final KeyService keyService;
  private final MessageService messageService;

  private Key key;

  private List<Message> messages;

  /**
   * 
   */
  @Inject
  public RevertDeleteKeyCommand(ProjectService projectService, LocaleService localeService,
      KeyService keyService, MessageService messageService) {
    this.projectService = projectService;
    this.localeService = localeService;
    this.keyService = keyService;
    this.messageService = messageService;
  }

  /**
   * @param key
   * @return
   */
  @Override
  public RevertDeleteKeyCommand with(models.Key key) {
    this.key = Key.from(key);
    this.messages = key.messages.stream().map(m -> Message.from(m)).collect(Collectors.toList());
    return this;
  }

  @Override
  public void execute() {
    Project project = projectService.byId(key.projectId);

    models.Key model = key.toModel(project);
    keyService.save(model);
    key.id = model.id;

    Map<String, Locale> locales =
        localeService.findBy(new LocaleCriteria().withProjectId(project.id)).getList().stream()
            .collect(toMap(l -> l.name, a -> a));

    messageService.save(
        messages.stream().map(m -> m.toModel(locales.get(m.localeName), model)).collect(toList()));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getMessage() {
    return Context.current().messages().at("key.deleted", key.name);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Call redirect() {
    return routes.Projects.keys(key.projectId, Keys.DEFAULT_SEARCH, Keys.DEFAULT_ORDER,
        Keys.DEFAULT_LIMIT, Keys.DEFAULT_OFFSET);
  }
}
