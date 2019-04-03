package actors;

import actors.ActivityProtocol.Activities;
import actors.ActivityProtocol.Activity;
import akka.actor.UntypedActor;
import dto.Dto;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import models.LogEntry;
import org.slf4j.LoggerFactory;
import repositories.LogEntryRepository;

@Singleton
public class ActivityActor extends UntypedActor {

  public static final String NAME = "activity-actor";

  private final LogEntryRepository logEntryRepository;

  @Inject
  public ActivityActor(LogEntryRepository logEntryRepository) {
    this.logEntryRepository = logEntryRepository;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void onReceive(Object msg) throws Throwable {
    if (msg instanceof Activity) {
      Activity<Dto> t = (Activity<Dto>) msg;

      LoggerFactory.getLogger(ActivityActor.class).debug("onReceive({})", t);

      logEntryRepository.create(ActivityActor.fromActivity(t));
    } else if (msg instanceof Activities) {
      Activities<Dto> t = (Activities<Dto>) msg;

      LoggerFactory.getLogger(ActivityActor.class).debug("onReceive({})", t.activities);

      logEntryRepository.save(t.activities.stream().map(ActivityActor::fromActivity).collect(
          Collectors.toList()));
    }
  }

  private static <T extends Dto> LogEntry fromActivity(Activity<T> t) {
    return LogEntry.from(t.type, t.user, t.project, t.dtoClass, t.before, t.after);
  }
}
