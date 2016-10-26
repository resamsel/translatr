package dto;

import java.util.UUID;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class User extends Dto
{
	public UUID id;

	@JsonIgnore
	public DateTime whenCreated;

	@JsonIgnore
	public DateTime whenUpdated;

	public String name;

	private User(models.User in)
	{
		this.id = in.id;
		this.whenCreated = in.whenCreated;
		this.whenUpdated = in.whenUpdated;
		this.name = in.name;
	}

	public models.User toModel()
	{
		models.User out = new models.User();

		out.whenCreated = whenCreated;
		out.whenUpdated = whenUpdated;
		out.name = name;

		return out;
	}

	/**
	 * @param project
	 * @return
	 */
	public static User from(models.User in)
	{
		return new User(in);
	}
}
