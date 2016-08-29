package commands;

import java.util.stream.Collectors;

import com.avaje.ebean.Ebean;

import controllers.routes;
import dto.Locale;
import models.Project;
import play.mvc.Call;
import play.mvc.Http.Context;

public class RevertDeleteLocaleCommand implements Command
{
	private Locale locale;

	public RevertDeleteLocaleCommand(models.Locale locale)
	{
		this.locale = Locale.from(locale);
	}

	@Override
	public void execute()
	{
		Project project = Project.byId(locale.projectId);

		models.Locale model = locale.toModel(project);
		Ebean.save(model);
		locale.id = model.id;

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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Call redirect()
	{
		return routes.Application.projectLocales(locale.projectId);
	}
}
