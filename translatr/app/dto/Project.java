package dto;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.joda.time.DateTime;

import criterias.MessageCriteria;

public class Project
{
	public UUID id;

	public DateTime whenCreated;

	public DateTime whenUpdated;

	public String name;

	public List<Key> keys;

	public List<Locale> locales;

	public List<Message> messages;

	public Project(models.Project project)
	{
		this.id = project.id;
		this.whenCreated = project.whenCreated;
		this.whenUpdated = project.whenUpdated;
		this.name = project.name;
		this.keys = project.keys.stream().map(k -> new Key(k)).collect(Collectors.toList());
		this.locales = project.locales.stream().map(l -> new Locale(l)).collect(Collectors.toList());
		this.messages = models.Message
			.findBy(new MessageCriteria().withProjectId(project.id))
			.stream()
			.map(m -> new Message(m))
			.collect(Collectors.toList());
	}

	public models.Project toModel()
	{
		models.Project model = new models.Project();

		model.whenCreated = whenCreated;
		model.whenUpdated = whenUpdated;
		model.name = name;

		return model;
	}
}
