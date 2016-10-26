package exporters;

import java.util.Collections;

import models.Locale;
import models.Message;

/**
 * 
 * <p>
 *
 * @author resamsel
 * @version 7 Oct 2016
 */
public class GettextExporter extends AbstractExporter implements Exporter
{
	private static final String FILENAME = "message.po";

	private static final String KEY_PREFIX = "msgid \"";

	private static final String KEY_SUFFIX = "\"\n";

	private static final String MESSAGE_PREFIX = "msgstr \"";

	private static final String MESSAGE_SUFFIX = "\"\n\n";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte[] apply(Locale locale)
	{
		Collections.sort(locale.messages, (a, b) -> a.key.name.compareTo(b.key.name));

		StringBuilder sb = new StringBuilder();
		for(Message message : locale.messages)
			sb
				.append(KEY_PREFIX)
				.append(escape(message.key.name))
				.append(KEY_SUFFIX)
				.append(MESSAGE_PREFIX)
				.append(escape(message.value))
				.append(MESSAGE_SUFFIX);

		return sb.toString().getBytes();
	}

	/**
	 * @param s
	 * @return
	 */
	private String escape(String s)
	{
		return s.replace("\n", "\\n\"\n\"");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getFilename(Locale locale)
	{
		return FILENAME;
	}
}
