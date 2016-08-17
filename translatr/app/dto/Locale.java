package dto;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.joda.time.DateTime;

import models.Project;

public class Locale {
	public UUID id;

	public DateTime whenCreated;

	public DateTime whenUpdated;

	public UUID projectId;

	public String name;

	public List<Message> messages;

	public Locale(models.Locale locale) {
		this.id = locale.id;
		this.whenCreated = locale.whenCreated;
		this.whenUpdated = locale.whenUpdated;
		this.projectId = locale.project.id;
		this.name = locale.name;
		this.messages = locale.messages.stream().map(m -> new Message(m)).collect(Collectors.toList());
	}

	public models.Locale toModel() {
		models.Locale model = new models.Locale();

		model.id = id;
		model.whenCreated = whenCreated;
		model.whenUpdated = whenUpdated;
		model.project = new Project();
		model.project.id = projectId;
		model.name = name;

		return model;
	}
}
