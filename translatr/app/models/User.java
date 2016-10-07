package models;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.joda.time.DateTime;

import com.avaje.ebean.Model.Find;
import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 29 Sep 2016
 */
@Entity
@Table(name = "user_", uniqueConstraints = {@UniqueConstraint(columnNames = {"email"})})
public class User
{
	public static final int USERNAME_LENGTH = 32;

	public static final int NAME_LENGTH = 32;

	public static final int EMAIL_LENGTH = 255;

	@Id
	public UUID id;

	@Version
	public Long version;

	public boolean deleted;

	@JsonIgnore
	@CreatedTimestamp
	public DateTime whenCreated;

	@JsonIgnore
	@UpdatedTimestamp
	public DateTime whenUpdated;

	@Column(nullable = false, length = USERNAME_LENGTH)
	public String username;

	@Column(nullable = false, length = NAME_LENGTH)
	public String name;

	@Column(nullable = false, length = EMAIL_LENGTH)
	public String email;

	private static final Find<UUID, User> find = new Find<UUID, User>()
	{
	};

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
