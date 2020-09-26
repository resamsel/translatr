package importers;

import models.Locale;
import play.mvc.Http;

import java.io.File;

public interface Importer {
	void apply(File file, Locale locale, Http.Request request) throws Exception;
}
