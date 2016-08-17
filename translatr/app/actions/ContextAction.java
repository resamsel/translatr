package actions;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import models.Project;
import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.Result;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 17 Aug 2016
 */
public class ContextAction extends Action.Simple
{
	/**
	 * {@inheritDoc}
	 */
	@Override
	public CompletionStage<Result> call(Context ctx)
	{
		if(ctx.session().containsKey("projectId"))
		{
			Project project = Project.find.byId(UUID.fromString(ctx.session().get("projectId")));

			if(project != null)
				ctx.args.put("locales", project.locales);
			else
				ctx.args.put("locales", Collections.emptyList());
		}
		else
		{
			ctx.args.put("locales", Collections.emptyList());
		}

		return delegate.call(ctx);
	}
}
