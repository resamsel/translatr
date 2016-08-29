package dto;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import models.Project;

public class Locale extends Dto
{
	public UUID id;

	@JsonIgnore
	public DateTime whenCreated;

	@JsonIgnore
	public DateTime whenUpdated;

	public UUID projectId;

	public String name;

	@JsonIgnore
	public List<Message> messages;

	private Locale(models.Locale locale)
	{
		this.id = locale.id;
		this.whenCreated = locale.whenCreated;
		this.whenUpdated = locale.whenUpdated;
		this.projectId = locale.project.id;
		this.name = locale.name;
		this.messages = locale.messages.stream().map(m -> Message.from(m)).collect(Collectors.toList());
	}

	public models.Locale toModel(Project project)
	{
		models.Locale model = new models.Locale();

		model.whenCreated = whenCreated;
		model.whenUpdated = whenUpdated;
		model.project = project;
		model.name = name;

		return model;
	}

	/**
	 * @param locale
	 * @return
	 */
	public static Locale from(models.Locale locale)
	{
		return new Locale(locale);
	}
}
