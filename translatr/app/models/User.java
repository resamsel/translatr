package models;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import org.joda.time.DateTime;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model.Find;
import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;

@Entity
@Table(name = "user_")
public class User
{
	public static final int USERNAME_LENGTH = 32;

	public static final int NAME_LENGTH = 32;

	public static final int EMAIL_LENGTH = 255;

	@Id
	public UUID id;

	@Version
	public Long version;

	@CreatedTimestamp
	public DateTime whenCreated;

	@UpdatedTimestamp
	public DateTime whenUpdated;

	public boolean active;

	@Column(nullable = false, length = USERNAME_LENGTH)
	public String username;

	@Column(nullable = false, length = NAME_LENGTH)
	public String name;

	@Column(nullable = false, length = EMAIL_LENGTH, unique = false)
	public String email;

	public boolean emailValidated;

	@OneToMany(cascade = CascadeType.ALL)
	public List<LinkedAccount> linkedAccounts;

	private static final Find<UUID, User> find = new Find<UUID, User>()
	{
	};

	public static boolean existsByAuthUserIdentity(final AuthUserIdentity identity)
	{
		final ExpressionList<User> exp = getAuthUserFind(identity);
		return exp.findRowCount() > 0;
	}

	private static ExpressionList<User> getAuthUserFind(final AuthUserIdentity identity)
	{
		return find.where().eq("active", true).eq("linkedAccounts.providerUserId", identity.getId()).eq(
			"linkedAccounts.providerKey",
			identity.getProvider());
	}

	public static User findByAuthUserIdentity(final AuthUserIdentity identity)
	{
		if(identity == null)
			return null;

		return getAuthUserFind(identity).findUnique();
	}

	public void merge(final User otherUser)
	{
		for(final LinkedAccount acc : otherUser.linkedAccounts)
		{
			this.linkedAccounts.add(LinkedAccount.create(acc));
		}
		// do all other merging stuff here - like resources, etc.

		// deactivate the merged user that got added to this one
		otherUser.active = false;
		Ebean.save(Arrays.asList(new User[]{otherUser, this}));
	}

	public static void merge(final AuthUser oldUser, final AuthUser newUser)
	{
		User.findByAuthUserIdentity(oldUser).merge(User.findByAuthUserIdentity(newUser));
	}

	public Set<String> getProviders()
	{
		final Set<String> providerKeys = new HashSet<String>(linkedAccounts.size());
		for(final LinkedAccount acc : linkedAccounts)
		{
			providerKeys.add(acc.providerKey);
		}
		return providerKeys;
	}

	public static User findByEmail(final String email)
	{
		return getEmailUserFind(email).findUnique();
	}

	private static ExpressionList<User> getEmailUserFind(final String email)
	{
		return find.where().eq("active", true).eq("email", email);
	}

	public LinkedAccount getAccountByProvider(final String providerKey)
	{
		return LinkedAccount.findByProviderKey(this, providerKey);
	}

	public User withId(UUID id)
	{
		this.id = id;
		return this;
	}

	public static User byId(UUID id)
	{
		return find.byId(id);
	}
}
