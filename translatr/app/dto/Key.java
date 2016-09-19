package dto;

import java.util.UUID;

import org.joda.time.DateTime;

import models.Project;

public class Key extends Dto
{
	public UUID id;

	public DateTime whenCreated;

	public DateTime whenUpdated;

	public UUID projectId;

	public String name;

	private Key(models.Key in)
	{
		this.id = in.id;
		this.whenCreated = in.whenCreated;
		this.whenUpdated = in.whenUpdated;
		this.projectId = in.project.id;
		this.name = in.name;
	}

	public models.Key toModel(Project project)
	{
		models.Key out = new models.Key();

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
