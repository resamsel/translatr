package dto;

import java.util.UUID;

import org.joda.time.DateTime;

import models.Project;
import models.ProjectRole;
import models.User;

public class ProjectUser extends Dto
{
	public UUID projectId;

	public UUID userId;

	public ProjectRole role;

	public DateTime whenCreated;

	public DateTime whenUpdated;

	private ProjectUser(models.ProjectUser in)
	{
		this.projectId = in.project.id;
		this.userId = in.user.id;
		this.role = in.role;
		this.whenCreated = in.whenCreated;
		this.whenUpdated = in.whenUpdated;
	}

	public models.ProjectUser toModel()
	{
		models.ProjectUser out = new models.ProjectUser();

		out.project = new Project().withId(projectId);
		out.user = new User().withId(userId);
		out.role = role;
		out.whenCreated = whenCreated;
		out.whenUpdated = whenUpdated;

		return out;
	}

	public static ProjectUser from(models.ProjectUser in)
	{
		return new ProjectUser(in);
	}
}
