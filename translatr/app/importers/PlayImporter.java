package importers;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.Ebean;

import models.Key;
import models.Locale;
import models.Message;
import play.libs.Json;

public class PlayImporter implements Importer
{
	private static final Logger LOGGER = LoggerFactory.getLogger(PlayImporter.class);

	@Override
	public void apply(File file, Locale locale) throws Exception
	{
		// TODO Auto-generated method stub
		LOGGER.debug("Importing from file {}", file.getName());
		for(String line : IOUtils.readLines(new FileInputStream(file), Charset.forName("utf-8")))
		{
			LOGGER.debug("Read line: {}", line);
			Line l = parseLine(line);
			LOGGER.debug("Parsed line: {}", l);

			saveMessage(locale, l);
		}
		LOGGER.debug("Imported from file {}", file.getName());
	}

	private Message saveMessage(Locale locale, Line line)
	{
		if(line == null)
			return null;

		Key key = Key.byProjectAndName(locale.project, line.key);
		if(key == null)
		{
			key = new Key(locale.project, line.key);

			LOGGER.debug("Key: {}", Json.toJson(key));

			Ebean.save(key);
		}

		Message message = Message.byKeyAndLocale(key, locale);
		if(message == null)
		{
			message = new Message(locale, key, line.value);
			Ebean.save(message);
		}
		else
		{
			message.value = line.value;
		}

		return message;
	}

	private Line parseLine(String line)
	{
		if(line.startsWith("#"))
			return null;

		String[] parts = line.split("\\s*=\\s*", 2);
		if(parts.length == 2)
			return new Line(parts[0], parts[1]);
		if(parts.length == 1)
			return new Line(parts[0]);

		return null;
	}

	static class Line
	{
		String key;

		String value;

		public Line(String key)
		{
			this(key, "");
		}

		public Line(String key, String value)
		{
			this.key = key;
			this.value = value;
		}

		@Override
		public String toString()
		{
			return "Line [key=" + key + ", value=" + value + "]";
		}
	}
}
