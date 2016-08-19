package dto;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.joda.time.DateTime;

import models.Project;

public class Key
{
	public UUID id;

	public DateTime whenCreated;

	public DateTime whenUpdated;

	public UUID projectId;

	public String name;

	public List<Message> messages;

	public Key(models.Key key)
	{
		this.id = key.id;
		this.whenCreated = key.whenCreated;
		this.whenUpdated = key.whenUpdated;
		this.projectId = key.project.id;
		this.name = key.name;
		this.messages = key.messages.stream().map(m -> new Message(m)).collect(Collectors.toList());
	}

	public models.Key toModel(Project project)
	{
		models.Key model = new models.Key();

		model.whenCreated = whenCreated;
		model.whenUpdated = whenUpdated;
		model.project = project;
		model.name = name;

		return model;
	}
}
