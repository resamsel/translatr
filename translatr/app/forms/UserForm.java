package forms;

import models.User;
import play.data.validation.Constraints;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 2 Sep 2016
 */
public class UserForm
{
	@Constraints.Required
	@Constraints.MaxLength(User.NAME_LENGTH)
	private String name;

	@Constraints.MaxLength(User.USERNAME_LENGTH)
	private String username;

	@Constraints.MaxLength(User.EMAIL_LENGTH)
	private String email;

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	/**
	 * @param in
	 * @return
	 */
	public User into(User in)
	{
		in.name = name;
		in.username = username;
		in.email = email;

		return in;
	}

	/**
	 * @param key
	 * @return
	 */
	public static UserForm from(User in)
	{
		UserForm out = new UserForm();

		out.name = in.name;
		out.username = in.username;
		out.email = in.email;

		return out;
	}
}
