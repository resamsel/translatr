package services.api.impl;

import com.fasterxml.jackson.databind.JsonNode;
import criterias.ProjectUserCriteria;
import mappers.ProjectUserMapper;
import models.ProjectUser;
import models.Scope;
import play.libs.Json;
import services.PermissionService;
import services.ProjectUserService;
import services.api.ProjectUserApiService;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author resamsel
 * @version 29 Jan 2017
 */
@Singleton
public class ProjectUserApiServiceImpl extends
    AbstractApiService<ProjectUser, Long, ProjectUserCriteria, ProjectUserService, dto.ProjectUser>
    implements ProjectUserApiService {

  @Inject
  protected ProjectUserApiServiceImpl(ProjectUserService projectUserService, PermissionService permissionService) {
    super(projectUserService, dto.ProjectUser.class, ProjectUserMapper::toDto,
        new Scope[]{Scope.ProjectRead, Scope.MemberRead},
        new Scope[]{Scope.ProjectRead, Scope.MemberWrite},
        permissionService
    );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected ProjectUser toModel(JsonNode json) {
    dto.ProjectUser dto = Json.fromJson(json, dto.ProjectUser.class);

    return ProjectUserMapper.toModel(dto);
  }
}
