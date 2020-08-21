package controllers;

import org.apache.http.client.utils.URIBuilder;
import play.mvc.Call;
import play.mvc.Http.RequestBuilder;
import tests.AbstractTest;

import java.net.URISyntaxException;

import static assertions.CustomAssertions.assertThat;
import static play.test.Helpers.route;

abstract class ApiControllerTest extends AbstractTest {

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
              .statusIsEqualTo(403);
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
