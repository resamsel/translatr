package services.impl;

import java.io.IOException;

import io.getstream.client.exception.StreamClientException;
import io.getstream.client.model.activities.SimpleActivity;
import models.LogEntry;
import models.Project;
import models.User;
import services.NotificationService;

/**
 * @author resamsel
 * @version 19 May 2017
 */
public class NotificationServiceDummy implements NotificationService {
  /**
   * {@inheritDoc}
   */
  @Override
  public void follow(User user, Project project) throws IOException, StreamClientException {}

  /**
   * {@inheritDoc}
   */
  @Override
  public void unfollow(User user, Project project) throws IOException, StreamClientException {}

  /**
   * {@inheritDoc}
   */
  @Override
  public SimpleActivity publish(User user, Project project, LogEntry logEntry)
      throws IOException, StreamClientException {
    return null;
  }
}
