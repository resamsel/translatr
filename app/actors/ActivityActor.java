package actors;

import actors.ActivityProtocol.Activities;
import actors.ActivityProtocol.Activity;
import akka.actor.AbstractActor;
import dto.Dto;
import models.LogEntry;
import org.slf4j.LoggerFactory;
import repositories.LogEntryRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.stream.Collectors;

@Singleton
public class ActivityActor extends AbstractActor {

  public static final String NAME = "activity-actor";

  private final LogEntryRepository logEntryRepository;

  @Inject
  public ActivityActor(LogEntryRepository logEntryRepository) {
    this.logEntryRepository = logEntryRepository;
  }

  private static <T extends Dto> LogEntry fromActivity(Activity<T> t) {
    return LogEntry.from(t.type, t.user, t.project, t.dtoClass, t.before, t.after);
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder()
            .match(Activity.class, t -> {
              LoggerFactory.getLogger(ActivityActor.class).debug("onReceive({})", t);

              logEntryRepository.create(ActivityActor.fromActivity(t));
            })
            .match(Activities.class, (t) -> {
              LoggerFactory.getLogger(ActivityActor.class).debug("onReceive({})", t.activities);

              logEntryRepository.save(((Activities<?>)t).activities.stream().map(ActivityActor::fromActivity).collect(
                      Collectors.toList()));
            })
            .build();
  }
}
