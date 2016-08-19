package models;

import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.joda.time.DateTime;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model.Find;
import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;

import criterias.LocaleCriteria;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"project_id", "name"})})
public class Locale
{
	@Id
	public UUID id;

	@CreatedTimestamp
	public DateTime whenCreated;

	@UpdatedTimestamp
	public DateTime whenUpdated;

	@ManyToOne(optional = false)
	public Project project;

	@Column(nullable = false, length = 15)
	public String name;

	@JsonIgnore
	@OneToMany
	public List<Message> messages;

	public Locale()
	{
	}

	public Locale(Project project, String name)
	{
		this.project = project;
		this.name = name;
	}

	private static final Find<UUID, Locale> find = new Find<UUID, Locale>()
	{
	};

	/**
	 * @param fromString
	 * @return
	 */
	public static Locale byId(UUID id)
	{
		return find.setId(id).fetch("project").findUnique();
	}

	/**
	 * @param project
	 * @return
	 */
	public static List<Locale> byProject(Project project)
	{
		return find.fetch("project").where().eq("project", project).findList();
	}

	/**
	 * @param project
	 * @param name
	 * @return
	 */
	public static Locale byProjectAndName(Project project, String name)
	{
		return find.fetch("project").where().eq("project", project).eq("name", name).findUnique();
	}

	/**
	 * @param criteria
	 * @return
	 */
	public static List<Locale> findBy(LocaleCriteria criteria)
	{
		ExpressionList<Locale> query = find.fetch("project").where();

		if(criteria.getProjectId() != null)
			query.eq("project.id", criteria.getProjectId());

		if(criteria.getLocaleName() != null)
			query.eq("name", criteria.getLocaleName());

		return query.findList();
	}

	public static List<Locale> last(Project project, int limit)
	{
		return find
			.fetch("project")
			.where()
			.eq("project", project)
			.order("whenUpdated desc")
			.setMaxRows(limit)
			.findList();
	}

	/**
	 * @param locale
	 */
	public static void delete(Locale locale)
	{
		for(Message message : Message.byLocale(locale.id))
			Ebean.delete(message);
		Ebean.delete(locale);
	}
}
