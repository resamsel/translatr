package integration.controllers;

import controllers.Projects;
import play.mvc.Call;
import play.mvc.Http.RequestBuilder;
import tests.AbstractTest;

import static assertions.CustomAssertions.assertThat;
import static play.test.Helpers.route;

/**
 * Created by resamsel on 24/07/2017.
 */
abstract class ControllerTest extends AbstractTest {

  void assertAccessDenied(Call call, String description) {
    assertThat(route(app, call)).as(description).statusIsEqualTo(Projects.SEE_OTHER);
  }

  void assertAccessDenied(RequestBuilder requestBuilder, String description) {
    assertThat(route(app, requestBuilder)).as(description).statusIsEqualTo(Projects.SEE_OTHER);
  }
}
