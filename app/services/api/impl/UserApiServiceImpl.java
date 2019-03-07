package services.api.impl;

import com.avaje.ebean.PagedList;
import com.fasterxml.jackson.databind.JsonNode;
import criterias.HasNextPagedList;
import criterias.LogEntryCriteria;
import criterias.UserCriteria;
import dto.DtoPagedList;
import models.Scope;
import models.User;
import play.libs.Json;
import services.LogEntryService;
import services.PermissionService;
import services.UserService;
import services.api.UserApiService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.UUID;

/**
 * @author resamsel
 * @version 29 Jan 2017
 */
@Singleton
public class UserApiServiceImpl extends
    AbstractApiService<User, UUID, UserCriteria, UserService, dto.User>
    implements UserApiService {

  private final LogEntryService logEntryService;

  @Inject
  protected UserApiServiceImpl(
      UserService userService,
      PermissionService permissionService,
      LogEntryService logEntryService) {
    super(userService, dto.User.class, dto.User::from,
        new Scope[]{Scope.UserRead},
        new Scope[]{Scope.UserWrite},
        permissionService);

    this.logEntryService = logEntryService;
  }

  @Override
  public dto.User byUsername(String username, String... propertiesToFetch) {
    return dtoMapper.apply(service.byUsername(username, propertiesToFetch));
  }

  @Override
  public PagedList<dto.Aggregate> activity(UUID id) {
    return new DtoPagedList<>(
        logEntryService.getAggregates(new LogEntryCriteria().withUserId(id)),
        dto.Aggregate::from);
  }

  @Override
  public dto.User me() {
    return dtoMapper.apply(User.loggedInUser());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected User toModel(JsonNode json) {
    dto.User dto = Json.fromJson(json, dto.User.class);

    return dto.toModel(service.byId(dto.id));
  }
}
