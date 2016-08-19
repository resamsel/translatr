package commands;

import java.util.stream.Collectors;

import com.avaje.ebean.Ebean;

import dto.Key;
import models.Project;
import play.mvc.Http.Context;

public class RevertDeleteKeyCommand implements Command
{
	private Key key;

	public RevertDeleteKeyCommand(models.Key key)
	{
		this.key = new Key(key);
	}

	@Override
	public void execute()
	{
		Project project = Project.byId(key.projectId);
		Ebean.save(key.toModel(project));
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
}
