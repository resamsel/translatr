package utils;

import com.feth.play.module.pa.PlayAuthenticate;

import models.User;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 7 Sep 2016
 */
public class Template
{
	public final PlayAuthenticate auth;

	public final User loggedInUser;

	public String title;

	public String name;

	/**
	 * @param auth
	 */
	private Template(PlayAuthenticate auth, User loggedInUser)
	{
		this.auth = auth;
		this.loggedInUser = loggedInUser;
	}

	public Template withTitle(String title)
	{
		this.title = title;
		return this;
	}

	public Template withName(String name)
	{
		this.name = name;
		return this;
	}

	public static Template create(PlayAuthenticate auth, User user)
	{
		return new Template(auth, user);
	}

	public static Template from(String title, String name)
	{
		return new Template(null, null).withTitle(title).withName(name);
	}

	public static Template from(String title)
	{
		return new Template(null, null).withTitle(title);
	}
}
