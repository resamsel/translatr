package integration.controllers;

import static assertions.ResultAssert.assertThat;
import static play.test.Helpers.route;

import controllers.Projects;
import java.net.URISyntaxException;
import org.apache.http.client.utils.URIBuilder;
import play.mvc.Call;
import play.mvc.Http.RequestBuilder;

abstract class ApiControllerTest extends ControllerTest {

  void assertAccessDenied(Call call, String accessToken, String description) {
    try {
      assertThat(route(
          app,
          new RequestBuilder().uri(
              new URIBuilder(call.url())
                  .addParameter("access_token", accessToken)
                  .build()
          )
      ))
          .as(description)
          .statusIsEqualTo(Projects.FORBIDDEN);
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
