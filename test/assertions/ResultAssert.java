package assertions;

import akka.stream.Materializer;
import play.mvc.Result;
import play.test.Helpers;

/**
 * Created by resamsel on 24/07/2017.
 */
public class ResultAssert extends AbstractGenericAssert<ResultAssert, Result> {

  private String content;

  ResultAssert(Result actual) {
    super("result", actual, ResultAssert.class);
  }

  public ResultAssert statusIsEqualTo(int expected) {
    return isEqualTo(
        expected, actual.status(),
        "Expected %s's %s to be <%s> but was <%s> with headers <%s> (%s)",
        name, "status", expected, actual.status(), actual.headers(), descriptionText()
    );
  }

  public ResultAssert contentTypeIsEqualTo(String expected) {
    return isEqualTo("contentType", expected, actual.contentType().get());
  }

  public ResultAssert charsetIsEqualTo(String expected) {
    return isEqualTo("charset", expected, actual.charset().get());
  }

  public ResultAssert headerIsEqualTo(String header, String expected) {
    return isEqualTo("header:" + header, expected, actual.header(header).get());
  }

  public ResultAssert contentIsEqualTo(String expected, Materializer materializer) {
    return isEqualTo("content", expected, content(materializer));
  }

  public ResultAssert contentContains(String expected, Materializer materializer) {
    return contains("content", expected, content(materializer));
  }

  private String content(Materializer materializer) {
    if (content == null) {
      content = Helpers.contentAsString(actual, materializer);
    }
    return content;
  }
}
