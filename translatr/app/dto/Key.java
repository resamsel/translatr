package dto;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.Version;

import org.joda.time.DateTime;

import models.Project;

public class Key extends Dto
{
	public UUID id;

	@Version
	public Long version;

	public DateTime whenCreated;

	public DateTime whenUpdated;

	public UUID projectId;

	public String name;

	public List<Message> messages;

	private Key(models.Key in)
	{
		this.id = in.id;
		this.version = in.version;
		this.whenCreated = in.whenCreated;
		this.whenUpdated = in.whenUpdated;
		this.projectId = in.project.id;
		this.name = in.name;
		this.messages = in.messages.stream().map(m -> Message.from(m)).collect(Collectors.toList());
	}

	public models.Key toModel(Project project)
	{
		models.Key out = new models.Key();

		out.version = version;
		out.whenCreated = whenCreated;
		out.whenUpdated = whenUpdated;
		out.project = project;
		out.name = name;

		return out;
	}

	public static Key from(models.Key key)
	{
		return new Key(key);
	}
}
