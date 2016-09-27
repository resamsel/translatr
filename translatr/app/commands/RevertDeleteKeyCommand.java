package commands;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import controllers.routes;
import criterias.LocaleCriteria;
import dto.Key;
import dto.Message;
import models.Locale;
import models.Project;
import play.mvc.Call;
import play.mvc.Http.Context;
import services.KeyService;
import services.MessageService;

public class RevertDeleteKeyCommand implements Command<models.Key>
{
	private Key key;

	private List<Message> messages;

	private final KeyService keyService;

	private final MessageService messageService;

	/**
	 * 
	 */
	@Inject
	public RevertDeleteKeyCommand(KeyService keyService, MessageService messageService)
	{
		this.keyService = keyService;
		this.messageService = messageService;
	}

	/**
	 * @param key
	 * @return
	 */
	@Override
	public RevertDeleteKeyCommand with(models.Key key)
	{
		this.key = Key.from(key);
		this.messages = key.messages.stream().map(m -> Message.from(m)).collect(Collectors.toList());
		return this;
	}

	@Override
	public void execute()
	{
		Project project = Project.byId(key.projectId);

		models.Key model = key.toModel(project);
		keyService.save(model);
		key.id = model.id;

		Map<String, Locale> locales = Locale.findBy(new LocaleCriteria().withProjectId(project.id)).stream().collect(
			groupingBy(l -> l.name, reducing(null, a -> a, (a, b) -> b)));

		messageService
			.save(messages.stream().map(m -> m.toModel(locales.get(m.localeName), model)).collect(Collectors.toList()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getMessage()
	{
		return Context.current().messages().at("key.deleted", key.name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Call redirect()
	{
		return routes.Projects.keys(key.projectId);
	}
}
