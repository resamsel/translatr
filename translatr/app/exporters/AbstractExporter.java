package exporters;

import models.Locale;
import play.mvc.Http.Response;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 7 Oct 2016
 */
public abstract class AbstractExporter implements Exporter
{
	@Override
	public Exporter addHeaders(Response response, Locale locale)
	{
		response.setHeader("Cache-Control", "public");
		response.setHeader("Content-Description", "File Transfer");
		response.setHeader("Content-Disposition", String.format("attachment; filename=%s", getFilename(locale)));
		response.setHeader("Content-Type", "mime/type");
		response.setHeader("Content-Transfer-Encoding", "binary");

		return this;
	}
}
