package commands;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import controllers.Keys;
import controllers.routes;
import criterias.LocaleCriteria;
import dto.Key;
import dto.Message;
import models.Locale;
import models.Project;
import play.inject.Injector;
import play.mvc.Call;
import play.mvc.Http.Context;
import services.KeyService;
import services.LocaleService;
import services.MessageService;
import services.ProjectService;

public class RevertDeleteKeyCommand implements Command<models.Key> {
  private static final long serialVersionUID = 6162300697203071900L;

  private Key key;

  private List<Message> messages;

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
  public void execute(Injector injector) {
    Project project = injector.instanceOf(ProjectService.class).byId(key.projectId);

    models.Key model = key.toModel(project);
    injector.instanceOf(KeyService.class).save(model);
    key.id = model.id;

    Map<String, Locale> locales = injector.instanceOf(LocaleService.class)
        .findBy(new LocaleCriteria().withProjectId(project.id)).getList().stream()
        .collect(toMap(l -> l.name, a -> a));

    injector.instanceOf(MessageService.class).save(
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
    return routes.Projects.keysBy(key.projectOwnerUsername, key.projectPath, Keys.DEFAULT_SEARCH,
        Keys.DEFAULT_ORDER, Keys.DEFAULT_LIMIT, Keys.DEFAULT_OFFSET);
  }

  /**
   * @param key
   * @return
   */
  public static RevertDeleteKeyCommand from(models.Key key) {
    return new RevertDeleteKeyCommand().with(key);
  }
}
