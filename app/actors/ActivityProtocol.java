package actors;

import dto.Dto;
import models.ActionType;
import models.Project;
import models.User;
import play.libs.Json;

import java.util.List;

public class ActivityProtocol {

  public static class Activity<T extends Dto> {

    public ActionType type;
    public User user;
    public Project project;
    public Class<T> dtoClass;
    public T before;
    public T after;

    public Activity(ActionType type, User user, Project project, Class<T> dtoClass, T before, T after) {
      this.type = type;
      this.user = user;
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
