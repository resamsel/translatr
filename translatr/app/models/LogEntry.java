package models;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.joda.time.DateTime;

import com.avaje.ebean.annotation.CreatedTimestamp;

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
	public Project project;

	@Column(length = 1024 * 1024)
	public String before;

	@Column(length = 1024 * 1024)
	public String after;

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
		out.project = project;
		out.contentType = clazz.getName();
		out.before = Json.stringify(Json.toJson(before));
		out.after = Json.stringify(Json.toJson(after));

		return out;
	}
}
