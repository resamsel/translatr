package services.api.impl;

import io.ebean.PagedList;
import criterias.LogEntryCriteria;
import dto.AuthorizationException;
import dto.DtoPagedList;
import dto.PermissionException;
import mappers.ActivityMapper;
import mappers.AggregateMapper;
import models.LogEntry;
import models.Scope;
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
    super(logEntryService, dto.Activity.class, ActivityMapper::toDto,
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
  protected LogEntry toModel(dto.Activity in) {
    return ActivityMapper.toModel(in, service.byId(in.id));
  }
}
