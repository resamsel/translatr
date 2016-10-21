package forms;

import models.Locale;
import play.data.validation.Constraints;

/**
 * 
 * <p>
 *
 * @author resamsel
 * @version 2 Sep 2016
 */
public class LocaleForm
{
	@Constraints.Required
	@Constraints.MaxLength(Locale.NAME_LENGTH)
	private String name;

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

	/**
	 * @param in
	 * @return
	 */
	public Locale into(Locale in)
	{
		in.name = name;

		return in;
	}

	/**
	 * @param localeName
	 * @return
	 */
	public static LocaleForm from(Locale in)
	{
		LocaleForm out = new LocaleForm();

		out.name = in.name;

		return out;
	}
}
