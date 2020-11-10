package services;

import com.google.inject.ImplementedBy;
import criterias.NotificationCriteria;
import io.getstream.client.exception.StreamClientException;
import io.getstream.client.model.activities.AggregatedActivity;
import io.getstream.client.model.activities.SimpleActivity;
import io.getstream.client.model.beans.StreamResponse;
import models.ActionType;
import services.impl.NotificationServiceDummy;

import java.io.IOException;
import java.util.UUID;

@ImplementedBy(NotificationServiceDummy.class)
public interface NotificationService {
  boolean isEnabled();

  void follow(UUID userId, UUID projectId) throws IOException, StreamClientException;

  void unfollow(UUID userId, UUID projectId) throws IOException, StreamClientException;

  SimpleActivity publish(UUID id, ActionType type, String name, String contentId, UUID userId, UUID projectId)
      throws IOException, StreamClientException;

  StreamResponse<AggregatedActivity<SimpleActivity>> find(NotificationCriteria criteria)
      throws IOException, StreamClientException;
}
