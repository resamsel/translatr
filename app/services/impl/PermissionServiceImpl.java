package services.impl;

import auth.AccessTokenProfile;
import criterias.ProjectUserCriteria;
import dto.PermissionException;
import models.Project;
import models.ProjectRole;
import models.ProjectUser;
import models.Scope;
import models.User;
import org.pac4j.core.profile.CommonProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Http;
import services.AuthProvider;
import services.PermissionService;
import services.ProjectUserService;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static repositories.ProjectRepository.FETCH_MEMBERS;

@Singleton
public class PermissionServiceImpl implements PermissionService {

  private static final Logger LOGGER = LoggerFactory.getLogger(PermissionServiceImpl.class);

  private final ProjectUserService projectUserService;
  private final AuthProvider authProvider;

  @Inject
  public PermissionServiceImpl(ProjectUserService projectUserService, AuthProvider authProvider) {
    this.projectUserService = projectUserService;
    this.authProvider = authProvider;
  }

  @Override
  public boolean hasPermissionAny(Http.Request request, @Nonnull Project project, User user, ProjectRole... roles) {
    if (project.members != null && !project.members.isEmpty()) {
      return hasPermissionAny(project.members, user, Arrays.asList(roles));
    }

    return hasPermissionAny(project.id, user, roles);
  }

  @Override
  public boolean hasPermissionAny(Http.Request request, UUID projectId, ProjectRole... roles) {
    return hasPermissionAny(projectId, authProvider.loggedInUser(request), roles);
  }

  @Override
  public boolean hasPermissionAny(UUID projectId, User loggedInUser, ProjectRole... roles) {
    return hasPermissionAny(projectId, loggedInUser, Arrays.asList(roles));
  }

  @Override
  public boolean hasPermissionAny(UUID projectId, User user, Collection<ProjectRole> roles) {
    if (user == null) {
      return false;
    }

    if (user.isAdmin()) {
      return true;
    }

    return hasPermissionAny(
            projectUserService.findBy(
                    new ProjectUserCriteria()
                            .withProjectId(projectId)
                            .withFetches(FETCH_MEMBERS))
                    .getList(),
            user,
            roles);
  }

  @Override
  public boolean hasPermissionAll(Http.Request request, Scope... scopes) {
    Optional<CommonProfile> optionalProfile = authProvider.loggedInProfile(request);

    if (!optionalProfile.isPresent()) {
      // user is not logged-in
      return false;
    }

    CommonProfile profile = optionalProfile.get();
    if (profile instanceof AccessTokenProfile) {
      // user is logged-in through access token
      AccessTokenProfile accessTokenProfile = (AccessTokenProfile) profile;
      return hasPermissionAll(accessTokenProfile.getScope(), Arrays.asList(scopes));
    }

    // user is logged-in through authentication
    return true;
  }

  private static boolean hasPermissionAll(List<Scope> actual, List<Scope> expected) {
    return actual.containsAll(expected);
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
  public void checkPermissionAll(Http.Request request, String errorMessage, Scope... scopes) {
    if (!hasPermissionAll(request, scopes)) {
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
