package importers;

import java.io.File;
import models.Locale;

public interface Importer {
	void apply(File file, Locale locale) throws Exception;
}
