package services.api.impl;

import criterias.LogEntryCriteria;
import dto.Activity;
import dto.AuthorizationException;
import dto.DtoPagedList;
import dto.PermissionException;
import io.ebean.PagedList;
import mappers.ActivityMapper;
import mappers.AggregateMapper;
import models.LogEntry;
import models.Scope;
import play.mvc.Http;
import services.LogEntryService;
import services.PermissionService;
import services.api.ActivityApiService;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;
import java.util.UUID;

/**
 * @author resamsel
 * @version 29 Jan 2017
 */
@Singleton
public class ActivityApiServiceImpl extends
    AbstractApiService<LogEntry, UUID, LogEntryCriteria, LogEntryService, dto.Activity>
    implements ActivityApiService {

  @Inject
  protected ActivityApiServiceImpl(
      LogEntryService logEntryService, PermissionService permissionService, Validator validator) {
    super(logEntryService, dto.Activity.class, (in, request) -> ActivityMapper.toDto(in),
        new Scope[]{Scope.ProjectRead},
        new Scope[]{Scope.ProjectWrite},
        permissionService,
        validator);
  }

  @Override
  public PagedList<dto.Aggregate> getAggregates(LogEntryCriteria criteria) {
    try {
      permissionService.checkPermissionAll(criteria.getRequest(), "Access token not allowed", readScopes);
    } catch (AuthorizationException | PermissionException e) {
      // Disallow custom criteria when not logged-in
      criteria = new LogEntryCriteria().withLimit(1000);
    }

    return new DtoPagedList<>(service.getAggregates(criteria), AggregateMapper::toDto);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected LogEntry toModel(Activity in, Http.Request request) {
    return ActivityMapper.toModel(in, service.byId(in.id, request));
  }
}
