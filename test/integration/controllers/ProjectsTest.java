package integration.controllers;

import controllers.Projects;
import controllers.routes;
import org.fest.assertions.api.Assertions;
import org.junit.Test;
import play.mvc.Result;
import play.test.Helpers;
import tests.AbstractTest;

/**
 * Created by resamsel on 10/07/2017.
 */
public class ProjectsTest extends AbstractTest {

  //@Test
  public void testIndex() {
    Result result = Helpers.route(routes.Projects.index("", "", 20, 0));

    Assertions.assertThat(result.status()).isEqualTo(Projects.OK);
    Assertions.assertThat(result.contentType().get()).isEqualTo("text/html");
    Assertions.assertThat(result.charset().get()).isEqualTo("utf-8");
    Assertions.assertThat(Helpers.contentAsString(result)).contains("ABC");
  }
}
