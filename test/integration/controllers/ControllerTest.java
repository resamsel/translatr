package integration.controllers;

import static assertions.ResultAssert.assertThat;
import static play.test.Helpers.route;

import controllers.Projects;
import play.mvc.Call;
import play.mvc.Http.RequestBuilder;
import tests.AbstractTest;

/**
 * Created by resamsel on 24/07/2017.
 */
public class ControllerTest extends AbstractTest {

  protected void assertAccessDenied(Call call, String description) {
    assertThat(route(app, call)).as(description).statusIsEqualTo(Projects.SEE_OTHER);
  }

  protected void assertAccessDenied(RequestBuilder requestBuilder, String description) {
    assertThat(route(app, requestBuilder)).as(description).statusIsEqualTo(Projects.SEE_OTHER);
  }
}
