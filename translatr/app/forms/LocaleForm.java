package forms;

import models.Locale;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 2 Sep 2016
 */
public class LocaleForm
{
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
	public Locale fill(Locale in)
	{
		in.name = name;

		return in;
	}
}
