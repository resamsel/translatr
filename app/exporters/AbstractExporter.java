package exporters;

import models.Locale;
import play.mvc.Result;

/**
 * @author resamsel
 * @version 7 Oct 2016
 */
public abstract class AbstractExporter implements Exporter {
  @Override
  public Result addHeaders(Result result, Locale locale) {
    return result.withHeader("Cache-Control", "public")
            .withHeader("Content-Description", "File Transfer")
            .withHeader("Content-Disposition", String.format("attachment; filename=%s", getFilename(locale)))
            .withHeader("Content-Type", "mime/type")
            .withHeader("Content-Transfer-Encoding", "binary");
  }
}
