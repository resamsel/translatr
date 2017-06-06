package models;

import be.objectify.deadbolt.java.models.Role;

/**
 *
 * @author resamsel
 * @version 3 Oct 2016
 */
public class UserRole implements Role
{
	private String name;

	/**
	 * @param name
	 */
	public UserRole(String name)
	{
		this.name = name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName()
	{
		return name;
	}

	public static final UserRole from(String name)
	{
		return new UserRole(name);
	}
}
