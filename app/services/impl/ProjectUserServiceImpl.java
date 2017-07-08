package services.impl;

import com.avaje.ebean.PagedList;
import criterias.ProjectUserCriteria;
import io.getstream.client.exception.StreamClientException;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;
import models.ActionType;
import models.LogEntry;
import models.ProjectUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.concurrent.HttpExecutionContext;
import services.KeyService;
import services.LocaleService;
import services.LogEntryService;
import services.NotificationService;
import services.ProjectUserService;

/**
 * @author resamsel
 * @version 29 Aug 2016
 */
@Singleton
public class ProjectUserServiceImpl extends
    AbstractModelService<ProjectUser, Long, ProjectUserCriteria> implements ProjectUserService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProjectUserServiceImpl.class);

  private final HttpExecutionContext executionContext;
  private final NotificationService notificationService;

  @Inject
  public ProjectUserServiceImpl(Validator validator, LocaleService localeService,
      KeyService keyService, LogEntryService logEntryService, HttpExecutionContext executionContext,
      NotificationService notificationService) {
    super(validator, logEntryService);

    this.executionContext = executionContext;
    this.notificationService = notificationService;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PagedList<ProjectUser> findBy(ProjectUserCriteria criteria) {
    return ProjectUser.findBy(criteria);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ProjectUser byId(Long id, String... fetches) {
    return ProjectUser.byId(id);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void prePersist(ProjectUser t, boolean update) {
    if (update) {
      logEntryService.save(LogEntry.from(ActionType.Update, t.project, dto.ProjectUser.class,
          toDto(byId(t.id)), toDto(t)));
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void postSave(ProjectUser t, boolean update) {
    if (!update) {
      logEntryService
          .save(LogEntry.from(ActionType.Create, t.project, dto.ProjectUser.class, null, toDto(t)));
    }

    CompletableFuture.runAsync(() -> {
      try {
        notificationService.follow(t.user, t.project);
      } catch (IOException | StreamClientException e) {
        LOGGER.error("Error while following project notification", e);
      }
    }, executionContext.current());

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
