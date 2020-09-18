package services;

import com.google.inject.ImplementedBy;
import models.Project;
import models.ProjectRole;
import models.ProjectUser;
import models.Scope;
import models.User;
import play.mvc.Http;
import services.impl.PermissionServiceImpl;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@ImplementedBy(PermissionServiceImpl.class)
public interface PermissionService {

  boolean hasPermissionAny(Http.Request request, Project project, User user, ProjectRole... roles);

  boolean hasPermissionAny(Http.Request request, UUID projectId, ProjectRole... roles);

  boolean hasPermissionAny(UUID projectId, User loggedInUser, ProjectRole... roles);

  boolean hasPermissionAny(UUID projectId, User user, Collection<ProjectRole> roles);

  boolean hasPermissionAll(Http.Request request, Scope... scopes);

  boolean hasPermissionAny(List<ProjectUser> members, User user,
                           Collection<ProjectRole> roles);

  void checkPermissionAll(Http.Request request, String errorMessage, Scope... scopes);
}
