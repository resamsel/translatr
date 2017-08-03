package services.impl;

import criterias.NotificationCriteria;
import io.getstream.client.exception.StreamClientException;
import io.getstream.client.model.activities.AggregatedActivity;
import io.getstream.client.model.activities.SimpleActivity;
import io.getstream.client.model.beans.StreamResponse;
import java.io.IOException;
import java.util.UUID;
import models.ActionType;
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
  public boolean isEnabled() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public StreamResponse<AggregatedActivity<SimpleActivity>> find(NotificationCriteria criteria) {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void follow(User user, Project project) throws IOException, StreamClientException {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void unfollow(User user, Project project) throws IOException, StreamClientException {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SimpleActivity publish(UUID id, ActionType type, String name, String contentId,
      UUID userId, UUID projectId)
      throws IOException, StreamClientException {
    return null;
  }
}
