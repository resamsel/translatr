package commands;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.Ebean;

import controllers.routes;
import dto.Project;
import play.mvc.Call;
import play.mvc.Http.Context;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 19 Aug 2016
 */
public class RevertDeleteProjectCommand implements Command
{
	private static final Logger LOGGER = LoggerFactory.getLogger(RevertDeleteProjectCommand.class);

	private final Project project;

	/**
	 * @param project
	 */
	public RevertDeleteProjectCommand(models.Project project)
	{
		this.project = Project.from(project);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute()
	{
		LOGGER.info("DTO Project #messages: {}", project.messages.size());

		models.Project model = models.Project.byId(project.id);
		Ebean.save(project.keys.stream().map(k -> k.toModel(model)).collect(Collectors.toList()));
		Ebean.save(project.locales.stream().map(l -> l.toModel(model)).collect(Collectors.toList()));
		Ebean.save(project.messages.stream().map(m -> m.toModel(model)).collect(Collectors.toList()));

		LOGGER.info("Before save project: deleted = {}", model.deleted);
		Ebean.save(model.withDeleted(false));
		LOGGER.info("After save project: deleted = {}", model.deleted);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getMessage()
	{
		return Context.current().messages().at("project.deleted", project.name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Call redirect()
	{
		return routes.Application.project(project.id);
	}
}
