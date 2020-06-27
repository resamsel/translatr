package services.impl;

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
import models.ActionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.Configuration;
import play.libs.Json;
import services.AuthProvider;
import services.NotificationService;
import utils.ConfigKey;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Singleton
public class NotificationServiceImpl implements NotificationService {
  private static final Logger LOGGER = LoggerFactory.getLogger(NotificationServiceImpl.class);

  private static final String FEED_GROUP_PROJECT = "project";
  private static final String FEED_SLUG_USER = "user";
  private static final String FEED_SLUG_LOG_ENTRY = "activity";
  private static final String FEED_GROUP_TIMELINE_AGGREGATED = "timeline_aggregated";

  private final StreamClient streamClient;
  private final AuthProvider authProvider;

  @Inject
  public NotificationServiceImpl(Configuration configuration, AuthProvider authProvider) {
    streamClient = new StreamClientImpl(new ClientConfiguration(),
        ConfigKey.StreamIOKey.getString(configuration),
        ConfigKey.StreamIOSecret.getString(configuration));
    this.authProvider = authProvider;
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
        streamClient.newFeed(FEED_GROUP_TIMELINE_AGGREGATED, authProvider.loggedInUser().id.toString());
    AggregatedActivityServiceImpl<SimpleActivity> activityService =
        feed.newAggregatedActivityService(SimpleActivity.class);

    FeedFilter filter =
        new FeedFilter.Builder().withLimit(criteria.getLimit() != null ? criteria.getLimit() : 10)
            .withOffset(criteria.getOffset() != null ? criteria.getOffset() : 0).build();

    return activityService.getActivities(filter);
  }

  @Override
  public void follow(UUID userId, UUID projectId) throws IOException, StreamClientException {
    LOGGER.debug("User {} follows project {}", userId, projectId);
    streamClient.newFeed(FEED_GROUP_TIMELINE_AGGREGATED, userId.toString())
        .follow(FEED_GROUP_PROJECT, projectId.toString());
  }

  @Override
  public void unfollow(UUID userId, UUID projectId) throws IOException, StreamClientException {
    streamClient.newFeed(FEED_GROUP_TIMELINE_AGGREGATED, userId.toString())
        .unfollow(FEED_GROUP_PROJECT, projectId.toString());
  }

  @Override
  public SimpleActivity publish(UUID id, ActionType type, String name, String contentId, UUID userId, UUID projectId)
      throws IOException, StreamClientException {
    Feed userFeed = createFeed(userId);
    FlatActivityServiceImpl<SimpleActivity> activityService =
        userFeed.newFlatActivityService(SimpleActivity.class);

    SimpleActivity activity = new SimpleActivity();

    activity.setActor(toId(FEED_SLUG_USER, userId));
    activity.setVerb(type.name().toLowerCase());
    activity.setObject(name);
    activity.setForeignId(toId(FEED_SLUG_LOG_ENTRY, id));
    activity.setTarget(contentId);
    LOGGER.debug("Creating activity: {}", Json.toJson(activity));

    List<String> feeds =
        Arrays.asList(toId(FEED_SLUG_USER, userId), toId(FEED_GROUP_TIMELINE_AGGREGATED, userId),
            toId(FEED_GROUP_TIMELINE_AGGREGATED, userId), toId(FEED_GROUP_PROJECT, projectId));
    LOGGER.debug("Adding activity to feeds: {}", feeds);

    return activityService.addActivityToMany(feeds, activity);
  }

  private Feed createFeed(UUID userId) throws InvalidFeedNameException {
    return streamClient.newFeed(FEED_SLUG_USER, userId.toString());
  }

  /**
   * @param slug the entity type
   * @param id   the ID of the slug
   */
  private String toId(String slug, UUID id) {
    return String.format("%s:%s", slug, id);
  }
}
