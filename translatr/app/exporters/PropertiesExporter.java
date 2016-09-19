package exporters;

import java.util.Collections;

import models.Locale;
import models.Message;
import play.mvc.Http.Response;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 30 Aug 2016
 */
public abstract class PropertiesExporter implements Exporter
{
	protected static final String DEFAULT = "default";

	@Override
	public byte[] apply(Locale locale)
	{
		Collections.sort(locale.messages, (a, b) -> a.key.name.compareTo(b.key.name));

		StringBuilder sb = new StringBuilder();
		for(Message message : locale.messages)
			sb.append(message.key.name).append(" = ").append(message.value).append("\n");

		return sb.toString().getBytes();
	}

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
