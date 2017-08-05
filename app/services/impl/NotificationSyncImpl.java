package services.impl;

import criterias.ProjectCriteria;
import io.getstream.client.exception.StreamClientException;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.concurrent.HttpExecutionContext;
import services.NotificationService;
import services.NotificationSync;
import services.ProjectService;

/**
 * @author resamsel
 * @version 19 May 2017
 */
@Singleton
public class NotificationSyncImpl implements NotificationSync {
  private static final Logger LOGGER = LoggerFactory.getLogger(NotificationSyncImpl.class);

  private final NotificationService notificationService;
  private final HttpExecutionContext executionContext;
  private final ProjectService projectService;

  @Inject
  public NotificationSyncImpl(NotificationService notificationService,
      HttpExecutionContext executionContext, ProjectService projectService) {
    this.notificationService = notificationService;
    this.executionContext = executionContext;
    this.projectService = projectService;

    init();
  }

  private void init() {
    CompletableFuture.runAsync(() -> {
      projectService.findBy(new ProjectCriteria()).getList().stream().forEach(project -> {
        project.members.stream().forEach(member -> {
          try {
            notificationService.follow(member.user.id, project.id);
          } catch (IOException | StreamClientException e) {
            LOGGER.error("Error while following project feed", e);
          }
        });
      });
    }, executionContext.current());
  }
}
