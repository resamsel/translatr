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

	@JsonIgnore
	public DateTime whenCreated;

	@JsonIgnore
	public DateTime whenUpdated;

	public UUID localeId;

	public String localeName;

	public UUID keyId;

	public String keyName;

	public String value;

	private Message(models.Message message)
	{
		this.id = message.id;
		this.whenCreated = message.whenCreated;
		this.whenUpdated = message.whenUpdated;
		if(message.locale != null)
		{
			this.localeId = message.locale.id;
			this.localeName = message.locale.name;
		}
		if(message.key != null)
		{
			this.keyId = message.key.id;
			this.keyName = message.key.name;
		}
		this.value = message.value;
	}

	public models.Message toModel(Project project)
	{
		models.Message model = new models.Message();

		model.whenCreated = whenCreated;
		model.whenUpdated = whenUpdated;
		model.locale = Locale.byProjectAndName(project, localeName);
		model.key = Key.byProjectAndName(project, keyName);
		model.value = value;

		LOGGER.info("DTO Message toModel: {}", Json.toJson(model));

		return model;
	}

	public static Message from(models.Message message)
	{
		return new Message(message);
	}
}
