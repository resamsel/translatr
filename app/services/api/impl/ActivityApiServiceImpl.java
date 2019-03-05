package services.api.impl;

import com.fasterxml.jackson.databind.JsonNode;
import criterias.LogEntryCriteria;
import models.LogEntry;
import models.Scope;
import play.libs.Json;
import services.LogEntryService;
import services.PermissionService;
import services.api.ActivityApiService;

import javax.inject.Inject;
import javax.inject.Singleton;
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
      LogEntryService logEntryService, PermissionService permissionService) {
    super(logEntryService, dto.Activity.class, dto.Activity::from,
        new Scope[]{Scope.ProjectRead},
        new Scope[]{Scope.ProjectWrite},
        permissionService);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected LogEntry toModel(JsonNode json) {
    dto.Activity dto = Json.fromJson(json, dto.Activity.class);

    return dto.toModel(service.byId(dto.id));
  }
}
