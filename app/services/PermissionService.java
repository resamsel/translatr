package services;

import com.google.inject.ImplementedBy;
import models.*;
import services.impl.PermissionServiceImpl;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@ImplementedBy(PermissionServiceImpl.class)
public interface PermissionService {

  boolean hasPermissionAny(Project project, ProjectRole... roles);

  boolean hasPermissionAny(UUID projectId, ProjectRole... roles);

  boolean hasPermissionAny(UUID projectId, User loggedInUser, ProjectRole... roles);

  boolean hasPermissionAny(UUID projectId, User user, Collection<ProjectRole> roles);

  boolean hasPermissionAll(Scope... scopes);

  boolean hasPermissionAll(AccessToken accessToken, Scope... scopes);

  boolean hasPermissionAny(AccessToken accessToken, Scope... scopes);

  boolean hasPermissionAny(List<ProjectUser> members, User user,
      Collection<ProjectRole> roles);

  void checkPermissionAll(String errorMessage, Scope... scopes);
}
