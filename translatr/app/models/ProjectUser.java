package models;

import static utils.Stopwatch.log;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model.Find;
import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;

import criterias.ProjectUserCriteria;

/**
 * 
 * <p>
 *
 * @author resamsel
 * @version 5 Oct 2016
 */
@Entity
public class ProjectUser
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ProjectUser.class);

	public static final int ROLE_LENGTH = 16;

	@Id
	public Long id;

	@Version
	public Long version;

	@JsonIgnore
	@CreatedTimestamp
	public DateTime whenCreated;

	@JsonIgnore
	@UpdatedTimestamp
	public DateTime whenUpdated;

	@ManyToOne
	public Project project;

	@ManyToOne
	public User user;

	@Enumerated(EnumType.STRING)
	@Column(length = ROLE_LENGTH)
	public ProjectRole role;

	/**
	 * 
	 */
	public ProjectUser()
	{
	}

	/**
	 * 
	 */
	public ProjectUser(ProjectRole role)
	{
		this.role = role;
	}

	private static final Find<Long, ProjectUser> find = new Find<Long, ProjectUser>()
	{
	};

	public ProjectUser withProject(Project project)
	{
		this.project = project;
		return this;
	}

	public ProjectUser withUser(User user)
	{
		this.user = user;
		return this;
	}

	public static ProjectUser byId(Long id)
	{
		return find.byId(id);
	}

	/**
	 * @param criteria
	 * @return
	 */
	private static ExpressionList<ProjectUser> findQuery(ProjectUserCriteria criteria)
	{
		ExpressionList<ProjectUser> query = find.where();

		if(criteria.getProjectId() != null)
			query.eq("project.id", criteria.getProjectId());

		if(criteria.getUserId() != null)
			query.eq("user.id", criteria.getUserId());

		criteria.paging(query);

		return query;
	}

	/**
	 * @param criteria
	 * @return
	 */
	public static List<ProjectUser> findBy(ProjectUserCriteria criteria)
	{
		return log(() -> findQuery(criteria).query().fetch("user").findList(), LOGGER, "findBy");
	}

	public static int countBy(ProjectUserCriteria criteria)
	{
		return findQuery(criteria).findRowCount();
	}
}
