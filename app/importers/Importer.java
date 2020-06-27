package importers;

import models.Locale;

import java.io.File;

public interface Importer {
	void apply(File file, Locale locale) throws Exception;
}
