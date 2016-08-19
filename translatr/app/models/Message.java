package models;

import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.joda.time.DateTime;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model.Find;
import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;

import criterias.MessageCriteria;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"locale_id", "key_id"})})
public class Message
{
	@Id
	public UUID id;

	@CreatedTimestamp
	public DateTime whenCreated;

	@UpdatedTimestamp
	public DateTime whenUpdated;

	@ManyToOne(optional = false)
	public Locale locale;

	@ManyToOne(optional = false)
	public Key key;

	@Column(nullable = false, length = 20 * 1024)
	public String value;

	public Message()
	{
	}

	public Message(Locale locale, Key key, String value)
	{
		this.locale = locale;
		this.key = key;
		this.value = value;
	}

	private static final Find<UUID, Message> find = new Find<UUID, Message>()
	{
	};

	public void updateFrom(Message in)
	{
		value = in.value;
	}

	/**
	 * @param fromString
	 * @return
	 */
	public static Message byId(UUID id)
	{
		return find.setId(id).fetch("key").fetch("locale").findUnique();
	}

	/**
	 * @param key
	 * @param locale
	 * @return
	 */
	public static Message byKeyAndLocale(Key key, Locale locale)
	{
		return find.fetch("key").fetch("locale").where().eq("key", key).eq("locale", locale).findUnique();
	}

	/**
	 * @param project
	 * @return
	 */
	public static int countByProject(Project project)
	{
		return find.fetch("key").fetch("locale").where().eq("key.project", project).findRowCount();
	}

	/**
	 * @param messageCriteria
	 * @return
	 */
	public static List<Message> findBy(MessageCriteria criteria)
	{
		ExpressionList<Message> query = Message.find.where();

		if(criteria.getProjectId() != null)
			query.eq("key.project.id", criteria.getProjectId());

		if(criteria.getKeyName() != null)
			query.eq("key.name", criteria.getKeyName());

		return query.findList();
	}

	/**
	 * @param localeId
	 * @param key
	 * @return
	 */
	public static Message byLocaleAndKeyName(UUID localeId, String key)
	{
		return find.where().eq("locale.id", localeId).eq("key.name", key).findUnique();
	}

	public static List<Message> last(Project project, int limit)
	{
		return find
			.fetch("key")
			.fetch("locale")
			.where()
			.eq("key.project", project)
			.order("whenUpdated desc")
			.setMaxRows(limit)
			.findList();
	}
}
