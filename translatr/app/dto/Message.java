package dto;

import java.util.UUID;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

import models.Key;
import models.Locale;
import models.Project;
import play.libs.Json;

public class Message extends Dto
{
	private static final Logger LOGGER = LoggerFactory.getLogger(Message.class);

	public UUID id;

	public Long version;

	@JsonIgnore
	public DateTime whenCreated;

	@JsonIgnore
	public DateTime whenUpdated;

	public UUID localeId;

	public String localeName;

	public UUID keyId;

	public String keyName;

	public String value;

	private Message(models.Message in)
	{
		this.id = in.id;
		this.version = in.version;
		this.whenCreated = in.whenCreated;
		this.whenUpdated = in.whenUpdated;
		this.localeId = in.locale.id;
		this.localeName = in.locale.name;
		this.keyId = in.key.id;
		this.keyName = in.key.name;
		this.value = in.value;
	}

	public models.Message toModel(Project project)
	{
		models.Message out = new models.Message();

		out.version = version;
		out.whenCreated = whenCreated;
		out.whenUpdated = whenUpdated;
		out.locale = Locale.byProjectAndName(project, localeName);
		out.key = Key.byProjectAndName(project, keyName);
		out.value = value;

		LOGGER.trace("DTO Message toModel: {}", Json.toJson(out));

		return out;
	}

	public static Message from(models.Message message)
	{
		return new Message(message);
	}
}
