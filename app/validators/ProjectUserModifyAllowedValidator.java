package validators;

import criterias.ProjectUserCriteria;
import io.ebean.PagedList;
import models.ProjectRole;
import models.ProjectUser;
import models.User;
import play.mvc.Http;
import repositories.ProjectUserRepository;
import services.AuthProvider;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Objects;

/**
 * @author resamsel
 * @version 23 Jan 2017
 */
@Singleton
public class ProjectUserModifyAllowedValidator implements CustomValidator<ProjectUser> {

  private final ProjectUserRepository projectUserRepository;
  private final AuthProvider authProvider;

  @Inject
  public ProjectUserModifyAllowedValidator(
      ProjectUserRepository projectUserRepository, AuthProvider authProvider) {
    this.projectUserRepository = projectUserRepository;
    this.authProvider = authProvider;
  }

  @Override
  public boolean isValid(ProjectUser model, Http.Request request) {
    if (model == null) {
      return false;
    }

    User loggedInUser = authProvider.loggedInUser(request);
    if (loggedInUser == null) {
      // Anonymous users are allowed to do nothing
      return false;
    }

    if (loggedInUser.isAdmin()) {
      // Admins are free to do anything
      return true;
    }

    if (model.project.owner != null && Objects.equals(loggedInUser.id, model.project.owner.id)) {
      // loggedInUser is owner
      return true;
    }

    PagedList<ProjectUser> found = projectUserRepository.findBy(
        new ProjectUserCriteria().withProjectId(model.project.id).withUserId(loggedInUser.id));
    if (found == null || found.getList().isEmpty()) {
      // Logged-in-user is not member of project
      return false;
    }

    ProjectUser member = found.getList().get(0);
    // Is the logged-in-user allowed to set the Owner role?
    // Owners are allowed to do anything
    return member.role == ProjectRole.Owner;
  }
}
