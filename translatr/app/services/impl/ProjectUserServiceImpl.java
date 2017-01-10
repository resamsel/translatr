package services.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.fasterxml.jackson.databind.JsonNode;

import models.ActionType;
import models.LogEntry;
import models.ProjectUser;
import play.Configuration;
import services.KeyService;
import services.LocaleService;
import services.LogEntryService;
import services.ProjectUserService;

/**
 *
 * @author resamsel
 * @version 29 Aug 2016
 */
@Singleton
public class ProjectUserServiceImpl extends AbstractModelService<ProjectUser, dto.ProjectUser>
    implements ProjectUserService {
  /**
   * 
   */
  @Inject
  public ProjectUserServiceImpl(Configuration configuration, LocaleService localeService,
      KeyService keyService, LogEntryService logEntryService) {
    super(dto.ProjectUser.class, configuration, logEntryService);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected ProjectUser byId(JsonNode id) {
    return ProjectUser.byId(id.asLong());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected ProjectUser toModel(dto.ProjectUser dto) {
    return dto.toModel();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void preSave(ProjectUser t, boolean update) {
    if (update)
      logEntryService.save(LogEntry.from(ActionType.Update, t.project, dto.ProjectUser.class,
          toDto(ProjectUser.byId(t.id)), toDto(t)));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void postSave(ProjectUser t, boolean update) {
    if (!update)
      logEntryService
          .save(LogEntry.from(ActionType.Create, t.project, dto.ProjectUser.class, null, toDto(t)));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void preDelete(ProjectUser t) {
    logEntryService
        .save(LogEntry.from(ActionType.Delete, t.project, dto.ProjectUser.class, toDto(t), null));
  }

  protected dto.ProjectUser toDto(ProjectUser t) {
    return dto.ProjectUser.from(t);
  }
}
