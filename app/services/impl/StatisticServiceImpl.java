package services.impl;

import criterias.LogEntryCriteria;
import criterias.ProjectCriteria;
import criterias.UserCriteria;
import dto.Statistic;
import play.mvc.Http;
import services.LogEntryService;
import services.ProjectService;
import services.StatisticService;
import services.UserService;

import javax.inject.Inject;
import javax.inject.Singleton;

import static repositories.impl.AbstractModelRepository.FETCH_COUNT;

@Singleton
public class StatisticServiceImpl implements StatisticService {
  private final UserService userService;
  private final ProjectService projectService;
  private final LogEntryService logEntryService;

  @Inject
  public StatisticServiceImpl(UserService userService, ProjectService projectService, LogEntryService logEntryService) {
    this.userService = userService;
    this.projectService = projectService;
    this.logEntryService = logEntryService;
  }

  @Override
  public Statistic find(Http.Request request) {
    int userCount = userService.findBy(UserCriteria.from(request).withFetches(FETCH_COUNT)).getTotalCount();
    int projectCount = projectService.findBy(ProjectCriteria.from(request).withFetches(FETCH_COUNT)).getTotalCount();
    int activityCount = logEntryService.findBy(LogEntryCriteria.from(request).withFetches(FETCH_COUNT)).getTotalCount();

    return Statistic.from(userCount, projectCount, activityCount);
  }
}
