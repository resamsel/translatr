package dto;

import java.util.UUID;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import models.Locale;

public class Message {
	public UUID id;

	@JsonIgnore
	public DateTime whenCreated;

	@JsonIgnore
	public DateTime whenUpdated;

	public UUID localeId;

	public UUID keyId;

	public String value;

	public Message(models.Message message) {
		this.id = message.id;
		this.whenCreated = message.whenCreated;
		this.whenUpdated = message.whenUpdated;
		this.localeId = message.locale.id;
		this.keyId = message.key.id;
		this.value = message.value;
	}

	public models.Message toModel() {
		models.Message model = new models.Message();

		model.id = id;
		model.whenCreated = whenCreated;
		model.whenUpdated = whenUpdated;
		model.locale = new Locale();
		model.locale.id = localeId;
		model.key = new models.Key();
		model.key.id = keyId;
		model.value = value;

		return model;
	}
}
