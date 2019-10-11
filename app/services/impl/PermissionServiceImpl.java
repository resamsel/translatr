package services.impl;

import criterias.ProjectUserCriteria;
import dto.AuthorizationException;
import dto.PermissionException;
import models.AccessToken;
import models.Project;
import models.ProjectRole;
import models.ProjectUser;
import models.Scope;
import models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.PermissionService;
import services.ProjectUserService;
import utils.ContextKey;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Singleton
public class PermissionServiceImpl implements PermissionService {

  private static final Logger LOGGER = LoggerFactory.getLogger(PermissionServiceImpl.class);

  private final ProjectUserService projectUserService;

  @Inject
  public PermissionServiceImpl(ProjectUserService projectUserService) {
    this.projectUserService = projectUserService;
  }

  @Override
  public boolean hasPermissionAny(@Nonnull Project project, ProjectRole... roles) {
    return hasPermissionAny(project, User.loggedInUser(), roles);
  }

  @Override
  public boolean hasPermissionAny(@Nonnull Project project, User user, ProjectRole... roles) {
    if (project.members != null) {
      return hasPermissionAny(project.members, user, Arrays.asList(roles));
    }

    return hasPermissionAny(project.id, user, roles);
  }

  @Override
  public boolean hasPermissionAny(UUID projectId, ProjectRole... roles) {
    return hasPermissionAny(projectId, User.loggedInUser(), roles);
  }

  @Override
  public boolean hasPermissionAny(UUID projectId, User loggedInUser, ProjectRole... roles) {
    return hasPermissionAny(projectId, loggedInUser, Arrays.asList(roles));
  }

  @Override
  public boolean hasPermissionAny(UUID projectId, User user, Collection<ProjectRole> roles) {
    if (user.isAdmin()) {
      return true;
    }

    return hasPermissionAny(
        projectUserService.findBy(new ProjectUserCriteria().withProjectId(projectId))
            .getList(), user, roles);
  }

  @Override
  public boolean hasPermissionAll(Scope... scopes) {
    return hasPermissionAll(ContextKey.AccessToken.get(), scopes);
  }

  @Override
  public boolean hasPermissionAll(AccessToken accessToken, Scope... scopes) {
    LOGGER.debug("Scopes of access token: {}, needed: {}",
        accessToken != null ? accessToken.getScopeList() : "-", scopes);

    if (accessToken == null) {
      return User.loggedInUser() != null;
    }

    // TODO: allow admin scopes also
    // !PermissionUtils.hasPermissionAny(Scope.ProjectRead, Scope.ProjectAdmin)
    // || !PermissionUtils.hasPermissionAny(Scope.LocaleRead, Scope.LocaleAdmin))

    return accessToken.getScopeList().containsAll(Arrays.asList(scopes));
  }

  @Override
  public boolean hasPermissionAny(AccessToken accessToken, Scope... scopes) {
    LOGGER.debug("Scopes of access token: {}, needed: {}",
        accessToken != null ? accessToken.getScopeList() : "-", scopes);

    if (accessToken == null) {
      return false;
    }

    List<Scope> scopeList = accessToken.getScopeList();
    scopeList.retainAll(Arrays.asList(scopes));

    return !scopeList.isEmpty();
  }

  @Override
  public boolean hasPermissionAny(List<ProjectUser> members, User user,
                                  Collection<ProjectRole> roles) {
    LOGGER.debug("Roles needed (any): {}", roles);

    for (ProjectUser member : members) {
      if (user.id.equals(member.user.id) && roles.contains(member.role)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public void checkPermissionAll(String errorMessage, Scope... scopes) {
    AccessToken accessToken = ContextKey.AccessToken.get();
    if (accessToken == null && User.loggedInUser() == null) {
      throw new AuthorizationException();
    }

    if (!hasPermissionAll(accessToken, scopes)) {
      throw new PermissionException(
          errorMessage,
          Arrays.stream(scopes)
              .map(Enum::name)
              .toArray(String[]::new
              )
      );
    }
  }
}
