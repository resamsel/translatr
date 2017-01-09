package utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import criterias.ProjectUserCriteria;
import models.AccessToken;
import models.Project;
import models.ProjectRole;
import models.ProjectUser;
import models.Scope;
import models.User;
import play.mvc.Http.Context;

/**
 * @author resamsel
 * @version 21 Oct 2016
 */
public class PermissionUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(PermissionUtils.class);

  public static boolean hasPermissionAll(Scope... scopes) {
    return hasPermissionAll((AccessToken) Context.current().args.get("accessToken"), scopes);
  }

  public static boolean hasPermissionAll(AccessToken accessToken, Scope... scopes) {
    LOGGER.debug("Scopes of access token: {}, needed: {}",
        accessToken != null ? accessToken.getScopeList() : "-", scopes);

    if (accessToken == null)
      return false;

    // TODO: allow admin scopes also
    // !PermissionUtils.hasPermissionAny(Scope.ProjectRead, Scope.ProjectAdmin)
    // || !PermissionUtils.hasPermissionAny(Scope.LocaleRead, Scope.LocaleAdmin))

    return accessToken.getScopeList().containsAll(Arrays.asList(scopes));
  }

  public static boolean hasPermissionAny(Scope... scopes) {
    return hasPermissionAny((AccessToken) Context.current().args.get("accessToken"), scopes);
  }

  public static boolean hasPermissionAny(AccessToken accessToken, Scope... scopes) {
    LOGGER.debug("Scopes of access token: {}, needed: {}",
        accessToken != null ? accessToken.getScopeList() : "-", scopes);

    if (accessToken == null)
      return false;

    List<Scope> scopeList = accessToken.getScopeList();
    scopeList.retainAll(Arrays.asList(scopes));

    return !scopeList.isEmpty();
  }

  /**
   * @param project
   * @param roles
   * @return
   */
  public static boolean hasPermissionAny(Project project, ProjectRole... roles) {
    return hasPermissionAny(project, User.loggedInUser(), roles);
  }

  /**
   * @param project
   * @param loggedInUser
   * @param roles
   * @return
   */
  public static boolean hasPermissionAny(Project project, User loggedInUser, ProjectRole... roles) {
    return hasPermissionAny(project, User.loggedInUser(), Arrays.asList(roles));
  }

  /**
   * @param project
   * @param user
   * @param roles
   * @return
   */
  public static boolean hasPermissionAny(Project project, User user,
      Collection<ProjectRole> roles) {
    LOGGER.debug("Members of project: {}, needed: {}", project != null ? project.members : "-",
        roles);

    for (ProjectUser member : ProjectUser
        .findBy(new ProjectUserCriteria().withProjectId(project.id)))
      if (user.id.equals(member.user.id) && roles.contains(member.role))
        return true;

    return false;
  }
}
