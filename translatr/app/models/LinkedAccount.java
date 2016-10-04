package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

import org.joda.time.DateTime;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model.Find;
import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import com.feth.play.module.pa.user.AuthUser;

import criterias.LinkedAccountCriteria;

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

	/**
	 * @param user
	 * @return
	 */
	public LinkedAccount update(final AuthUser user)
	{
		this.providerKey = user.getProvider();
		this.providerUserId = user.getId();
		return this;
	}

	/**
	 * @param id
	 * @return
	 */
	public static LinkedAccount byId(Long id)
	{
		return find.setId(id).findUnique();
	}

	/**
	 * @param user
	 * @param key
	 * @return
	 */
	public static LinkedAccount findByProviderKey(final User user, String key)
	{
		return find.where().eq("user", user).eq("providerKey", key).findUnique();
	}

	/**
	 * @param criteria
	 * @return
	 */
	public static List<LinkedAccount> findBy(LinkedAccountCriteria criteria)
	{
		ExpressionList<LinkedAccount> query = find.where();

		if(criteria.getUserId() != null)
			query.eq("user.id", criteria.getUserId());

		if(criteria.getLimit() != null)
			query.setMaxRows(criteria.getLimit() + 1);

		if(criteria.getOffset() != null)
			query.setFirstRow(criteria.getOffset());

		if(criteria.getOrder() != null)
			query.order(criteria.getOrder());
		else
			query.order("whenCreated");

		return query.findList();
	}

	public static LinkedAccount createFrom(final AuthUser user)
	{
		return new LinkedAccount().update(user);
	}
}
