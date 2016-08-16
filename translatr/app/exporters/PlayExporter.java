package exporters;

import java.util.Collections;

import models.Locale;
import models.Message;
import play.mvc.Http.Response;

public class PlayExporter implements Exporter {
	private static final String DEFAULT = "default";
	private static final String FILENAME_DEFAULT = "messages";
	private static final String FILENAME_FORMAT = "messages.%s";

	@Override
	public byte[] apply(Locale locale) {
		Collections.sort(locale.messages, (a, b) -> a.key.name.compareTo(b.key.name));

		StringBuilder sb = new StringBuilder();
		for (Message message : locale.messages)
			sb.append(message.key.name).append("=").append(message.value).append("\n");

		return sb.toString().getBytes();
	}

	@Override
	public Exporter addHeaders(Response response, Locale locale) {
		response.setHeader("Cache-Control", "public");
		response.setHeader("Content-Description", "File Transfer");
		response.setHeader("Content-Disposition",
				String.format("attachment; filename=%s", getFilename(locale)));
		response.setHeader("Content-Type", "mime/type");
		response.setHeader("Content-Transfer-Encoding", "binary");
		
		return this;
	}

	@Override
	public String getFilename(Locale locale) {
		if (DEFAULT.equals(locale.name))
			return FILENAME_DEFAULT;
		return String.format(FILENAME_FORMAT, locale.name);
	}
}
