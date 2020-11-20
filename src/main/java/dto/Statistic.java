package dto;

public class Statistic extends Dto {
  public long userCount;
  public long projectCount;
  public long activityCount;

  public static Statistic from(int userCount, int projectCount, int activityCount) {
    Statistic out = new Statistic();

    out.userCount = userCount;
    out.projectCount = projectCount;
    out.activityCount = activityCount;

    return out;
  }
}
