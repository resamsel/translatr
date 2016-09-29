package models;

import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.joda.time.DateTime;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model.Find;
import com.avaje.ebean.annotation.CreatedTimestamp;

import criterias.LogEntryCriteria;
import dto.Dto;
import play.libs.Json;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 29 Aug 2016
 */
@Entity
public class LogEntry
{
	@Id
	public UUID id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	public ActionType type;

	@Column(nullable = false, length = 64)
	public String contentType;

	@CreatedTimestamp
	@Column(nullable = false)
	public DateTime whenCreated;

	@ManyToOne(optional = false)
	@JoinColumn(name = "user_id")
	public User user;

	@ManyToOne
	public Project project;

	@Column(length = 1024 * 1024)
	public String before;

	@Column(length = 1024 * 1024)
	public String after;

	private static final Find<UUID, LogEntry> find = new Find<UUID, LogEntry>()
	{
	};

	/**
	 * @param type
	 * @param clazz
	 * @param after
	 * @param before
	 * @return
	 */
	public static <T extends Dto> LogEntry from(ActionType type, Project project, Class<T> clazz, T before, T after)
	{
		LogEntry out = new LogEntry();

		out.type = type;
		out.user = new User();
		out.user.id = UUID.fromString("1258D7C1-A1B5-40B0-A79B-0E8B64C7560A");
		out.project = project;
		out.contentType = clazz.getName();
		out.before = Json.stringify(Json.toJson(before));
		out.after = Json.stringify(Json.toJson(after));

		return out;
	}

	/**
	 * @param criteria
	 * @return
	 */
	public static List<LogEntry> findBy(LogEntryCriteria criteria)
	{
		ExpressionList<LogEntry> query = find.fetch("project").where();

		if(criteria.getProjectId() != null)
			query.eq("project.id", criteria.getProjectId());

		return query.order("whenCreated").findList();
	}
}
