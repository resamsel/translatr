package dto;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.Version;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import models.Project;

public class Locale extends Dto
{
	public UUID id;

	@Version
	public Long version;

	@JsonIgnore
	public DateTime whenCreated;

	@JsonIgnore
	public DateTime whenUpdated;

	public UUID projectId;

	public String name;

	@JsonIgnore
	public List<Message> messages;

	private Locale(models.Locale in)
	{
		this.id = in.id;
		this.version = in.version;
		this.whenCreated = in.whenCreated;
		this.whenUpdated = in.whenUpdated;
		this.projectId = in.project.id;
		this.name = in.name;
		this.messages = in.messages.stream().map(m -> Message.from(m)).collect(Collectors.toList());
	}

	public models.Locale toModel(Project project)
	{
		models.Locale out = new models.Locale();

		out.version = version;
		out.whenCreated = whenCreated;
		out.whenUpdated = whenUpdated;
		out.project = project;
		out.name = name;

		return out;
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
