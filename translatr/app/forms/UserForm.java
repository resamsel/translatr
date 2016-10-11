package forms;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import models.User;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;
import play.mvc.Http.Context;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 2 Sep 2016
 */
public class UserForm
{
	private UUID id;

	@Constraints.Required
	@Constraints.MaxLength(User.NAME_LENGTH)
	private String name;

	@Constraints.Required
	@Constraints.MaxLength(User.USERNAME_LENGTH)
	private String username;

	@Constraints.Required
	@Constraints.MaxLength(User.EMAIL_LENGTH)
	private String email;

	public UUID getId()
	{
		return id;
	}

	public void setId(UUID id)
	{
		this.id = id;
	}

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
	 * 
	 */
	public List<ValidationError> validate()
	{
		List<ValidationError> errors = new ArrayList<ValidationError>();

		User user = User.byUsername(username);
		if(user != null && !user.id.equals(id))
			errors.add(new ValidationError("username", Context.current().messages().at("user.username.duplicate")));

		return errors.isEmpty() ? null : errors;
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

		out.id = in.id;
		out.name = in.name;
		out.username = in.username;
		out.email = in.email;

		return out;
	}
}
