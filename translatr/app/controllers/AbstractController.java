package controllers;

import models.Locale;
import models.Project;
import play.mvc.Controller;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 16 Sep 2016
 */
public abstract class AbstractController extends Controller
{
	protected void select(Project project)
	{
		// session("projectId", project.id.toString());
		ctx().args.put("projectId", project.id);
	}

	protected void select(Locale locale)
	{
		// session("localeId", locale.id.toString());
		// session("localeName", locale.name);
	}
}
