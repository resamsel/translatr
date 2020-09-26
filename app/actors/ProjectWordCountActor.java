package actors;

import actors.WordCountProtocol.ChangeWordCount;
import akka.actor.AbstractActor;
import services.ProjectService;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author resamsel
 * @version 6 Jun 2017
 */
@Singleton
public class ProjectWordCountActor extends AbstractActor {
  public static final String NAME = "project-word-count-actor";

  private final ProjectService projectService;

  @Inject
  public ProjectWordCountActor(ProjectService projectService) {
    this.projectService = projectService;
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder()
            .match(ChangeWordCount.class,
                    wordCount -> projectService.increaseWordCountBy(wordCount.id, wordCount.wordCountDiff, null /* FIXME */))
            .build();
  }
}
