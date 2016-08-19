package commands;

import java.util.stream.Collectors;

import com.avaje.ebean.Ebean;

import dto.Locale;
import models.Project;
import play.mvc.Http.Context;

public class RevertDeleteLocaleCommand implements Command
{
	private Locale locale;

	public RevertDeleteLocaleCommand(models.Locale locale)
	{
		this.locale = new Locale(locale);
	}

	@Override
	public void execute()
	{
		Project project = Project.byId(locale.projectId);
		Ebean.save(locale.toModel(project));
		Ebean.save(locale.messages.stream().map(m -> m.toModel(project)).collect(Collectors.toList()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getMessage()
	{
		return Context.current().messages().at("locale.deleted", locale.name);
	}
}
