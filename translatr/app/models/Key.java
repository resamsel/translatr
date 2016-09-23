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

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model.Find;
import com.avaje.ebean.Query;
import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;

import controllers.routes;
import criterias.KeyCriteria;
import play.mvc.Http.Context;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"project_id", "name"})})
public class Key implements Suggestable
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value()
	{
		return Context.current().messages().at("key.autocomplete", name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Data data()
	{
		return Data.from(Key.class, name, routes.Application.key(id).absoluteURL(Context.current().request()));
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
		Query<Key> q = Key.find.fetch("project").alias("k");
		ExpressionList<Key> query = q.where();

		if(criteria.getProjectId() != null)
			query.eq("project.id", criteria.getProjectId());

		if(criteria.getSearch() != null)
			query
				.disjunction()
				.ilike("name", "%" + criteria.getSearch() + "%")
				.exists(
					Ebean
						.createQuery(Message.class)
						.where()
						.raw("key.id = k.id")
						.ilike("value", "%" + criteria.getSearch() + "%")
						.query())
				.endJunction();

		if(criteria.getMissing() == Boolean.TRUE)
		{
			ExpressionList<Message> messageQuery = Ebean.createQuery(Message.class).where().raw("key.id = k.id");

			if(criteria.getLocaleId() != null)
				messageQuery.eq("locale.id", criteria.getLocaleId());

			query.notExists(messageQuery.query());
		}

		if(criteria.getOffset() != null)
			query.setFirstRow(criteria.getOffset());

		if(criteria.getLimit() != null)
			query.setMaxRows(criteria.getLimit() + 1);

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
