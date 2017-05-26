package services.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

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
import play.libs.Json;
import services.NotificationService;
import utils.ConfigKey;
import utils.JsonUtils;

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
    streamClient.newFeed(FEED_SLUG_TIMELINE_AGGREGATED, user.id.toString())
        .follow(FEED_SLUG_PROJECT_AGGREGATED, project.id.toString());
  }

  @Override
  public void unfollow(User user, Project project) throws IOException, StreamClientException {
    streamClient.newFeed(FEED_SLUG_TIMELINE_AGGREGATED, user.id.toString())
        .unfollow(FEED_SLUG_PROJECT, project.id.toString());
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
    activity.setObject(JsonUtils.nameOf(logEntry));
    activity.setForeignId(toId(FEED_SLUG_LOG_ENTRY, logEntry));
    activity.setTarget(toContentId(logEntry));
    LOGGER.debug("Creating activity: {}", Json.toJson(activity));

    List<String> feeds = Arrays.asList(toId(FEED_SLUG_USER, user),
        toId(FEED_SLUG_TIMELINE_AGGREGATED, user), toId(FEED_SLUG_PROJECT_AGGREGATED, project));
    LOGGER.debug("Adding activity to feeds: {}", feeds);

    return activityService.addActivityToMany(feeds, activity);
  }

  private Feed createFeed(User user) throws InvalidFeedNameException {
    return streamClient.newFeed(FEED_SLUG_USER, user.id.toString());
  }

  /**
   * @param slug the entity type
   * @param model the model
   * @return
   */
  private String toId(String slug, Model<?, ?> model) {
    return String.format("%s:%s", slug, String.valueOf(model.getId()));
  }

  /**
   * @param slug the entity type
   * @param model the model
   * @return
   */
  private String toContentId(LogEntry logEntry) {
    String slug = logEntry.contentType.replaceAll("^.*\\.", "").toLowerCase();
    String id;
    switch (logEntry.type) {
      case Create:
      case Update:
      case Login:
      case Logout:
        id = Json.parse(logEntry.after).get("id").asText();
        break;
      case Delete:
        id = Json.parse(logEntry.before).get("id").asText();
        break;
      default:
        id = "0";
        break;
    }
    return String.format("%s:%s", slug, id);
  }
}
