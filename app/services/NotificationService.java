package services;

import java.io.IOException;

import criterias.NotificationCriteria;
import io.getstream.client.exception.StreamClientException;
import io.getstream.client.model.activities.AggregatedActivity;
import io.getstream.client.model.activities.SimpleActivity;
import io.getstream.client.model.beans.StreamResponse;
import models.LogEntry;
import models.Project;
import models.User;

public interface NotificationService {
  /**
   * @return
   */
  boolean isEnabled();

  /**
   * @param user
   * @param project
   * @throws IOException
   * @throws StreamClientException
   */
  void follow(User user, Project project) throws IOException, StreamClientException;

  /**
   * @param user
   * @param project
   * @throws IOException
   * @throws StreamClientException
   */
  void unfollow(User user, Project project) throws IOException, StreamClientException;

  /**
   * @param user
   * @param project
   * @param logEntry
   * @return
   * @throws IOException
   * @throws StreamClientException
   */
  SimpleActivity publish(User user, Project project, LogEntry logEntry)
      throws IOException, StreamClientException;

  /**
   * @param criteria
   * @return
   * @throws IOException
   * @throws StreamClientException
   */
  StreamResponse<AggregatedActivity<SimpleActivity>> find(NotificationCriteria criteria)
      throws IOException, StreamClientException;
}
