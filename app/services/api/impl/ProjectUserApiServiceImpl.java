package services.api.impl;

import criterias.ProjectUserCriteria;
import mappers.ProjectUserMapper;
import models.ProjectUser;
import models.Scope;
import play.mvc.Http;
import services.PermissionService;
import services.ProjectUserService;
import services.api.ProjectUserApiService;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;

/**
 * @author resamsel
 * @version 29 Jan 2017
 */
@Singleton
public class ProjectUserApiServiceImpl extends
    AbstractApiService<ProjectUser, Long, ProjectUserCriteria, ProjectUserService, dto.ProjectUser>
    implements ProjectUserApiService {

  @Inject
  protected ProjectUserApiServiceImpl(ProjectUserService projectUserService, PermissionService permissionService, Validator validator) {
    super(projectUserService, dto.ProjectUser.class, (in, request) -> ProjectUserMapper.toDto(in),
        new Scope[]{Scope.ProjectRead, Scope.MemberRead},
        new Scope[]{Scope.ProjectRead, Scope.MemberWrite},
        permissionService,
        validator
    );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected ProjectUser toModel(dto.ProjectUser in, Http.Request request) {
    return ProjectUserMapper.toModel(in);
  }
}
