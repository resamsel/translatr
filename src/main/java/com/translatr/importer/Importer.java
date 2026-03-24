package com.translatr.importer;

import com.translatr.model.Locale;
import java.io.InputStream;

public interface Importer {
    ImportResult apply(InputStream stream, Locale locale) throws Exception;
}
