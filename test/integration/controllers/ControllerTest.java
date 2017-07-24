package integration.controllers;

import static assertions.ResultAssert.assertThat;
import static play.test.Helpers.route;

import controllers.Projects;
import play.mvc.Call;
import tests.AbstractTest;

/**
 * Created by resamsel on 24/07/2017.
 */
public class ControllerTest extends AbstractTest {

  protected void assertAccessDenied(Call call, String description) {
    assertThat(route(app, call)).as(description).statusIsEqualTo(Projects.SEE_OTHER);
  }
}
