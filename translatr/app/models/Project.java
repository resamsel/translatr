package models;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.joda.time.DateTime;

import com.avaje.ebean.Model.Find;
import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;

import criterias.MessageCriteria;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"name"})})
public class Project
{
	// private static final Logger LOGGER = LoggerFactory.getLogger(Project.class);

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

	@Column(nullable = false)
	public String name;

	@JsonIgnore
	@OneToMany
	public List<Locale> locales;

	@JsonIgnore
	@OneToMany
	public List<Key> keys;

	@Transient
	private Long keysSize;

	@Transient
	private Long localesSize;

	@Transient
	private Map<UUID, Long> keysSizeMap;

	public Project()
	{
	}

	public Project(String name)
	{
		this.name = name;
	}

	private static final Find<UUID, Project> find = new Find<UUID, Project>()
	{
	};

	public static Project byId(UUID id)
	{
		return find.byId(id);
	}

	/**
	 * @return
	 */
	public static List<Project> all()
	{
		return find.where().eq("deleted", false).order("name").findList();
	}

	public float progress()
	{
		long keysSize = keysSize();
		long localesSize = localesSize();
		if(keysSize < 1 || localesSize < 1)
			return 0f;
		return (float)Message.countBy(this) / (float)(keysSize * localesSize);
	}

	public long missing(UUID localeId)
	{
		return keysSize() - keysSizeMap(localeId);
	}

	public long keysSizeMap(UUID localeId)
	{
		if(keysSizeMap == null)
			keysSizeMap = Message.findBy(new MessageCriteria().withProjectId(this.id)).stream().collect(
				groupingBy(m -> m.locale.id, counting()));

		return keysSizeMap.getOrDefault(localeId, 0l);
	}

	public long localesSize()
	{
		if(localesSize == null)
			localesSize = Locale.countBy(this);
		return localesSize;
	}

	public long keysSize()
	{
		if(keysSize == null)
			keysSize = Key.countBy(this);
		return keysSize;
	}

	public long messagesSize()
	{
		return Message.countBy(this);
	}

	public Project withName(String name)
	{
		this.name = name;
		return this;
	}

	public Project withDeleted(boolean deleted)
	{
		this.deleted = deleted;
		return this;
	}

	/**
	 * @param name
	 * @return
	 */
	public static Project byName(String name)
	{
		return find.where().eq("name", name).findUnique();
	}

	/**
	 * @param project
	 */
	public Project updateFrom(Project in)
	{
		name = in.name;

		return this;
	}
}
