package dto;

import java.util.UUID;
import models.User;
import org.joda.time.DateTime;

public class LinkedAccount extends Dto
{
	private static final long serialVersionUID = 2867985852818242361L;

  public UUID userId;

	public DateTime whenCreated;

	public DateTime whenUpdated;

	public String providerKey;

	public String providerUserId;

	private LinkedAccount(models.LinkedAccount in)
	{
		this.userId = in.user.id;
		this.providerKey = in.providerKey;
		this.providerUserId = in.providerUserId;
		this.whenCreated = in.whenCreated;
		this.whenUpdated = in.whenUpdated;
	}

	public models.LinkedAccount toModel()
	{
		models.LinkedAccount out = new models.LinkedAccount();

		out.user = new User().withId(userId);
		out.whenCreated = whenCreated;
		out.whenUpdated = whenUpdated;
		out.providerKey = providerKey;
		out.providerUserId = providerUserId;

		return out;
	}

	public static LinkedAccount from(models.LinkedAccount linkedAccount)
	{
		return new LinkedAccount(linkedAccount);
	}
}
