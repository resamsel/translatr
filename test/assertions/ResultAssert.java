package assertions;

import akka.stream.Materializer;
import play.mvc.Result;
import play.test.Helpers;

/**
 * Created by resamsel on 24/07/2017.
 */
public class ResultAssert extends AbstractGenericAssert<ResultAssert, Result> {

  private String content;

  private ResultAssert(Result actual) {
    super("result", actual, ResultAssert.class);
  }

  public static ResultAssert assertThat(Result actual) {
    return new ResultAssert(actual);
  }

  public ResultAssert statusIsEqualTo(int expected) {
    return isEqualTo("status", expected, actual.status());
  }

  public ResultAssert contentTypeIsEqualTo(String expected) {
    return isEqualTo("contentType", expected, actual.contentType().get());
  }

  public ResultAssert charsetIsEqualTo(String expected) {
    return isEqualTo("charset", expected, actual.charset().get());
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
