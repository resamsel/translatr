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

	private Project(models.Project in)
	{
		this.id = in.id;
		this.version = in.version;
		this.whenCreated = in.whenCreated;
		this.whenUpdated = in.whenUpdated;
		this.name = in.name;
		this.keys = in.keys.stream().map(k -> Key.from(k)).collect(Collectors.toList());
		this.locales = in.locales.stream().map(l -> Locale.from(l)).collect(Collectors.toList());
		this.messages = models.Message
			.findBy(new MessageCriteria().withProjectId(in.id))
			.stream()
			.map(m -> Message.from(m))
			.collect(Collectors.toList());
	}

	public models.Project toModel()
	{
		models.Project out = new models.Project();

		out.version = version;
		out.whenCreated = whenCreated;
		out.whenUpdated = whenUpdated;
		out.name = name;

		return out;
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
