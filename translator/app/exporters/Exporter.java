package exporters;

import models.Locale;
import play.mvc.Http.Response;

public interface Exporter {
	byte[] export(Locale locale);

	Exporter addHeaders(Response response, Locale locale);
	
	String getFilename(Locale locale);
}
