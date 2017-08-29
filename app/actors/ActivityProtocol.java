package actors;

import dto.Dto;
import java.util.List;
import models.ActionType;
import models.Project;
import play.libs.Json;

public class ActivityProtocol {

  public static class Activity<T extends Dto> {

    public ActionType type;
    public Project project;
    public Class<T> dtoClass;
    public T before;
    public T after;

    public Activity(ActionType type, Project project, Class<T> dtoClass, T before, T after) {
      this.type = type;
      this.project = project;
      this.dtoClass = dtoClass;
      this.before = before;
      this.after = after;
    }

    @Override
    public String toString() {
      return Json.stringify(Json.toJson(this));
    }
  }

  public static class Activities<T extends Dto> {

    public final List<Activity<T>> activities;

    public Activities(List<Activity<T>> activities) {
      this.activities = activities;
    }
  }
}
