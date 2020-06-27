package assertions;

import models.Locale;
import models.Project;
import models.ProjectUser;
import models.User;
import org.assertj.core.api.Assertions;
import play.mvc.Result;

public class CustomAssertions extends Assertions {

  public static ProjectAssert assertThat(Project actual) {
    return new ProjectAssert(actual);
  }

  public static LocaleAssert assertThat(Locale actual) {
    return new LocaleAssert(actual);
  }

  public static LocaleDtoAssert assertThat(dto.Locale actual) {
    return new LocaleDtoAssert(actual);
  }

  public static ResultAssert assertThat(Result actual) {
    return new ResultAssert(actual);
  }

  public static ProjectUserAssert assertThat(ProjectUser actual) {
    return new ProjectUserAssert(actual);
  }

  public static UserAssert assertThat(User actual) {
    return new UserAssert(actual);
  }

  public static UserDtoAssert assertThat(dto.User actual) {
    return new UserDtoAssert(actual);
  }
}
