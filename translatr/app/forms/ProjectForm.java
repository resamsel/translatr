package forms;

import models.Project;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 2 Sep 2016
 */
public class ProjectForm
{
	@Constraints.Required
	@Constraints.MaxLength(Project.NAME_LENGTH)
	private String name;

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * 
	 */
	public Project fill(Project in)
	{
		in.name = name;

		return in;
	}

	/**
	 * @param project
	 * @return
	 */
	public static ProjectForm from(Project in)
	{
		ProjectForm out = new ProjectForm();

		out.name = in.name;

		return out;
	}

	/**
	 * @param formFactory
	 * @return
	 */
	public static Form<ProjectForm> form(FormFactory formFactory)
	{
		return formFactory.form(ProjectForm.class);
	}
}
