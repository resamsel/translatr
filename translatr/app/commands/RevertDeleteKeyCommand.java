package commands;

import java.util.stream.Collectors;

import com.avaje.ebean.Ebean;

import controllers.routes;
import dto.Key;
import models.Project;
import play.mvc.Call;
import play.mvc.Http.Context;

public class RevertDeleteKeyCommand implements Command
{
	private Key key;

	public RevertDeleteKeyCommand(models.Key key)
	{
		this.key = Key.from(key);
	}

	@Override
	public void execute()
	{
		Project project = Project.byId(key.projectId);

		models.Key model = key.toModel(project);
		Ebean.save(model);
		key.id = model.id;

		Ebean.save(key.messages.stream().map(m -> m.toModel(project)).collect(Collectors.toList()));
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
		return routes.Application.projectKeys(key.projectId);
	}
}
