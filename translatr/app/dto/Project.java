package dto;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.joda.time.DateTime;

import criterias.MessageCriteria;

public class Project extends Dto
{
	public UUID id;

	public Long version;

	public DateTime whenCreated;

	public DateTime whenUpdated;

	public String name;

	public List<Key> keys;

	public List<Locale> locales;

	public List<Message> messages;

	private Project(models.Project project)
	{
		this.id = project.id;
		this.version = project.version;
		this.whenCreated = project.whenCreated;
		this.whenUpdated = project.whenUpdated;
		this.name = project.name;
		this.keys = project.keys.stream().map(k -> Key.from(k)).collect(Collectors.toList());
		this.locales = project.locales.stream().map(l -> Locale.from(l)).collect(Collectors.toList());
		this.messages = models.Message
			.findBy(new MessageCriteria().withProjectId(project.id))
			.stream()
			.map(m -> Message.from(m))
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

	/**
	 * @param project
	 * @return
	 */
	public static Project from(models.Project project)
	{
		return new Project(project);
	}
}
