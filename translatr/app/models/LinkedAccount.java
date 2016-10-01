package models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

import org.joda.time.DateTime;

import com.avaje.ebean.Model.Find;
import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import com.feth.play.module.pa.user.AuthUser;

@Entity
public class LinkedAccount
{
	@Id
	@GeneratedValue
	public Long id;

	@Version
	public Long version;

	@CreatedTimestamp
	public DateTime whenCreated;

	@UpdatedTimestamp
	public DateTime whenUpdated;

	@ManyToOne
	public User user;

	public String providerUserId;

	public String providerKey;

	public static final Find<Long, LinkedAccount> find = new Find<Long, LinkedAccount>()
	{
	};

	public static LinkedAccount findByProviderKey(final User user, String key)
	{
		return find.where().eq("user", user).eq("providerKey", key).findUnique();
	}

	public static LinkedAccount create(final AuthUser authUser)
	{
		final LinkedAccount ret = new LinkedAccount();
		ret.update(authUser);
		return ret;
	}

	public void update(final AuthUser authUser)
	{
		this.providerKey = authUser.getProvider();
		this.providerUserId = authUser.getId();
	}

	public static LinkedAccount create(final LinkedAccount acc)
	{
		final LinkedAccount ret = new LinkedAccount();
		ret.providerKey = acc.providerKey;
		ret.providerUserId = acc.providerUserId;

		return ret;
	}
}
