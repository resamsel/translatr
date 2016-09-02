package models;

import static utils.Stopwatch.log;

import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model.Find;
import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;

import criterias.KeyCriteria;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"project_id", "name"})})
public class Key
{
	private static final Logger LOGGER = LoggerFactory.getLogger(Key.class);

	@Id
	public UUID id;

	@Version
	public Long version;

	@CreatedTimestamp
	public DateTime whenCreated;

	@UpdatedTimestamp
	public DateTime whenUpdated;

	@ManyToOne(optional = false)
	public Project project;

	@Column(length = 255)
	public String name;

	@JsonIgnore
	@OneToMany
	public List<Message> messages;

	public Key()
	{
	}

	public Key(Project project, String name)
	{
		this.project = project;
		this.name = name;
	}

	private static final Find<UUID, Key> find = new Find<UUID, Key>()
	{
	};

	/**
	 * @param keyId
	 * @return
	 */
	public static Key byId(UUID id)
	{
		return find.setId(id).fetch("project").findUnique();
	}

	/**
	 * @param messageCriteria
	 * @return
	 */
	public static List<Key> findBy(KeyCriteria criteria)
	{
		ExpressionList<Key> query = Key.find.fetch("project").where();

		if(criteria.getProjectId() != null)
			query.eq("project.id", criteria.getProjectId());

		if(criteria.getLimit() != null)
			query.setMaxRows(criteria.getLimit());

		if(criteria.getOrder() != null)
			query.setOrderBy(criteria.getOrder());

		return log(() -> query.findList(), LOGGER, "Retrieving keys");
	}

	/**
	 * @param project
	 * @param name
	 * @return
	 */
	public static Key byProjectAndName(Project project, String name)
	{
		return find.fetch("project").where().eq("project", project).eq("name", name).findUnique();
	}

	public static List<Key> last(Project project, int limit)
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
	 * @param project
	 * @return
	 */
	public static long countBy(Project project)
	{
		return log(() -> find.where().eq("project", project).findRowCount(), LOGGER, "countBy");
	}
}
