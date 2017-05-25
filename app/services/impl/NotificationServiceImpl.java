package services.impl;

import java.io.IOException;
import java.util.Arrays;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import criterias.NotificationCriteria;
import io.getstream.client.StreamClient;
import io.getstream.client.apache.StreamClientImpl;
import io.getstream.client.config.ClientConfiguration;
import io.getstream.client.exception.InvalidFeedNameException;
import io.getstream.client.exception.StreamClientException;
import io.getstream.client.model.activities.AggregatedActivity;
import io.getstream.client.model.activities.SimpleActivity;
import io.getstream.client.model.beans.StreamResponse;
import io.getstream.client.model.feeds.Feed;
import io.getstream.client.model.filters.FeedFilter;
import io.getstream.client.service.AggregatedActivityServiceImpl;
import io.getstream.client.service.FlatActivityServiceImpl;
import models.LogEntry;
import models.Model;
import models.Project;
import models.User;
import play.Configuration;
import services.NotificationService;
import utils.ConfigKey;

@Singleton
public class NotificationServiceImpl implements NotificationService {
  private static final Logger LOGGER = LoggerFactory.getLogger(NotificationServiceImpl.class);

  private static final String FEED_SLUG_PROJECT = "project";
  private static final String FEED_SLUG_PROJECT_AGGREGATED = "project_aggregated";
  private static final String FEED_SLUG_USER = "user";
  private static final String FEED_SLUG_LOG_ENTRY = "activity";
  private static final String FEED_SLUG_TIMELINE_AGGREGATED = "timeline_aggregated";

  private final StreamClient streamClient;

  @Inject
  public NotificationServiceImpl(Configuration configuration) {
    streamClient = new StreamClientImpl(new ClientConfiguration(),
        ConfigKey.StreamIOKey.getString(configuration),
        ConfigKey.StreamIOSecret.getString(configuration));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isEnabled() {
    return true;
  }

  /**
   * {@inheritDoc}
   * 
   * @throws StreamClientException
   * @throws IOException
   */
  @Override
  public StreamResponse<AggregatedActivity<SimpleActivity>> find(NotificationCriteria criteria)
      throws IOException, StreamClientException {
    Feed feed =
        streamClient.newFeed(FEED_SLUG_TIMELINE_AGGREGATED, User.loggedInUser().id.toString());
    AggregatedActivityServiceImpl<SimpleActivity> activityService =
        feed.newAggregatedActivityService(SimpleActivity.class);

    FeedFilter filter =
        new FeedFilter.Builder().withLimit(criteria.getLimit() != null ? criteria.getLimit() : 10)
            .withOffset(criteria.getOffset() != null ? criteria.getOffset() : 0).build();

    return activityService.getActivities(filter);
  }

  @Override
  public void follow(User user, Project project) throws IOException, StreamClientException {
    LOGGER.debug("User {} follows project {}", user.username, project.name);
    createFeed(user).follow(FEED_SLUG_PROJECT, project.id.toString());
  }

  @Override
  public void unfollow(User user, Project project) throws IOException, StreamClientException {
    createFeed(user).unfollow(FEED_SLUG_PROJECT, project.id.toString());
  }

  @Override
  public SimpleActivity publish(User user, Project project, LogEntry logEntry)
      throws IOException, StreamClientException {
    Feed userFeed = createFeed(user);
    FlatActivityServiceImpl<SimpleActivity> activityService =
        userFeed.newFlatActivityService(SimpleActivity.class);

    SimpleActivity activity = new SimpleActivity();

    activity.setActor(toId(FEED_SLUG_USER, user));
    activity.setVerb(logEntry.type.name().toLowerCase());
    activity.setObject(toId(FEED_SLUG_LOG_ENTRY, logEntry));
    activity.setForeignId(toId(FEED_SLUG_LOG_ENTRY, logEntry));
    if (project != null)
      activity.setTarget(toId(FEED_SLUG_PROJECT, project));

    LOGGER.debug("Adding activity to feed user:{} and project_aggregated:{}", user.id, project.id);
    return activityService.addActivityToMany(Arrays.asList(toId(FEED_SLUG_USER, user),
        toId(FEED_SLUG_TIMELINE_AGGREGATED, user), toId(FEED_SLUG_PROJECT_AGGREGATED, project)),
        activity);
  }

  private Feed createFeed(User user) throws InvalidFeedNameException {
    return streamClient.newFeed(FEED_SLUG_USER, user.id.toString());
  }

  /**
   * @param user
   * @return
   */
  private String toId(String slug, Model<?, ?> model) {
    return String.format("%s:%s", slug, String.valueOf(model.getId()));
  }
}
