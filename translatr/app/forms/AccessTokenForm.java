package forms;

import models.AccessToken;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints;
import validators.AccessTokenByUserAndName;

/**
 * 
 * <p>
 *
 * @author resamsel
 * @version 2 Sep 2016
 */
public class AccessTokenForm
{
	@Constraints.Required
	@Constraints.MaxLength(AccessToken.NAME_LENGTH)
	@AccessTokenByUserAndName
	private String name;

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @param in
	 * @return
	 */
	public AccessToken fill(AccessToken in)
	{
		in.name = name;

		return in;
	}

	/**
	 * @param project
	 * @return
	 */
	public static AccessTokenForm from(AccessToken in)
	{
		AccessTokenForm out = new AccessTokenForm();

		out.name = in.name;

		return out;
	}

	/**
	 * @param formFactory
	 * @return
	 */
	public static Form<AccessTokenForm> form(FormFactory formFactory)
	{
		return formFactory.form(AccessTokenForm.class);
	}
}
