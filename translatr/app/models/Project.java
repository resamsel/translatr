package models;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.joda.time.DateTime;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Model.Find;
import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"name"})})
public class Project
{
	@Id
	public UUID id;

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
		return find.order("name").findList();
	}

	public float progress()
	{
		if(keys.size() < 1 || locales.size() < 1)
			return 0f;
		return (float)locales.stream().collect(Collectors.summingInt(l -> l.messages.size()))
			/ (float)(keys.size() * locales.size());
	}

	public float progress(Locale locale)
	{
		if(keys.size() < 1)
			return 0f;
		return (float)locale.messages.size() / (float)keys.size();
	}

	public float progress(Key key)
	{
		if(locales.size() < 1)
			return 0f;
		return (float)key.messages.size() / (float)locales.size();
	}

	public int missing(Locale locale)
	{
		return keys.size() - locale.messages.size();
	}

	public int messagesSize()
	{
		return Message.countByProject(this);
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
	public static void delete(Project project)
	{
		for(Key key : project.keys)
			Key.delete(key);
		for(Locale locale : project.locales)
			Locale.delete(locale);
		Ebean.delete(project);
	}
}
