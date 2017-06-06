package actors;

import javax.inject.Inject;
import javax.inject.Singleton;

import actors.WordCountProtocol.ChangeWordCount;
import akka.actor.UntypedActor;
import services.ProjectService;

/**
 * @author resamsel
 * @version 6 Jun 2017
 */
@Singleton
public class ProjectWordCountActor extends UntypedActor {
  public static final String NAME = "project-word-count-actor";

  private final ProjectService projectService;

  @Inject
  public ProjectWordCountActor(ProjectService projectService) {
    this.projectService = projectService;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onReceive(Object msg) throws Throwable {
    if (msg instanceof ChangeWordCount) {
      ChangeWordCount wordCount = (ChangeWordCount) msg;
      projectService.increaseWordCountBy(wordCount.id, wordCount.wordCountDiff);
    }
  }
}
