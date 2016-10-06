package commands;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import controllers.routes;
import dto.ProjectUser;
import play.mvc.Call;
import play.mvc.Http.Context;
import services.ProjectUserService;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 19 Aug 2016
 */
public class RevertDeleteProjectUserCommand implements Command<models.ProjectUser>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(RevertDeleteProjectUserCommand.class);

	private ProjectUser projectUser;

	private final ProjectUserService projectUserService;

	/**
	 * @param project
	 */
	@Inject
	public RevertDeleteProjectUserCommand(ProjectUserService projectUserService)
	{
		this.projectUserService = projectUserService;
	}

	/**
	 * @param projectUser
	 * @return
	 */
	@Override
	public RevertDeleteProjectUserCommand with(models.ProjectUser projectUser)
	{
		this.projectUser = ProjectUser.from(projectUser);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute()
	{
		models.ProjectUser model = projectUser.toModel();

		LOGGER.debug("Reverting member {} from project {}", projectUser.userName, projectUser.projectName);

		projectUserService.save(model);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getMessage()
	{
		return Context.current().messages().at("project.member.deleted", projectUser.userName, projectUser.projectName);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Call redirect()
	{
		return routes.Projects.members(projectUser.projectId);
	}
}
