package services;

import criterias.NotificationCriteria;
import io.getstream.client.exception.StreamClientException;
import io.getstream.client.model.activities.AggregatedActivity;
import io.getstream.client.model.activities.SimpleActivity;
import io.getstream.client.model.beans.StreamResponse;
import java.io.IOException;
import java.util.UUID;
import models.ActionType;
import models.LogEntry;
import models.Project;
import models.User;

public interface NotificationService {
  boolean isEnabled();

  void follow(User user, Project project) throws IOException, StreamClientException;

  void unfollow(User user, Project project) throws IOException, StreamClientException;

  SimpleActivity publish(UUID id, ActionType type, String name, String contentId, UUID userId, UUID projectId)
      throws IOException, StreamClientException;

  StreamResponse<AggregatedActivity<SimpleActivity>> find(NotificationCriteria criteria)
      throws IOException, StreamClientException;
}
