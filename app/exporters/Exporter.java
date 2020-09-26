package exporters;

import models.Locale;
import play.mvc.Result;

public interface Exporter {
	byte[] apply(Locale locale);

	Result addHeaders(Result result, Locale locale);

	String getFilename(Locale locale);
}
