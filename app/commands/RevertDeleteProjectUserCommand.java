package commands;

import dto.ProjectUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.inject.Injector;
import play.mvc.Call;
import play.mvc.Http.Context;
import services.ProjectUserService;

/**
 *
 * @author resamsel
 * @version 19 Aug 2016
 */
public class RevertDeleteProjectUserCommand implements Command<models.ProjectUser> {
  private static final long serialVersionUID = 2528906591019314536L;

  private static final Logger LOGGER =
      LoggerFactory.getLogger(RevertDeleteProjectUserCommand.class);

  private ProjectUser projectUser;

  /**
   * @param projectUser
   * @return
   */
  @Override
  public RevertDeleteProjectUserCommand with(models.ProjectUser projectUser) {
    this.projectUser = ProjectUser.from(projectUser);
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void execute(Injector injector) {
    models.ProjectUser model = projectUser.toModel();

    LOGGER.debug("Reverting member {} from project {}", projectUser.userUsername,
        projectUser.projectName);

    injector.instanceOf(ProjectUserService.class).save(model);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getMessage() {
    return Context.current().messages().at("project.member.deleted", projectUser.userUsername,
        projectUser.projectName);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Call redirect() {
    return projectUser.route();
  }

  /**
   * @param member
   * @return
   */
  public static RevertDeleteProjectUserCommand from(models.ProjectUser member) {
    return new RevertDeleteProjectUserCommand().with(member);
  }
}
