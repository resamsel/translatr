package actions;

import java.util.concurrent.CompletionStage;

import javax.inject.Inject;

import commands.Command;
import models.Project;
import models.User;
import play.cache.CacheApi;
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
	private final CacheApi cache;

	/**
	 * 
	 */
	@Inject
	public ContextAction(CacheApi cache)
	{
		this.cache = cache;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CompletionStage<Result> call(Context ctx)
	{
		Project brandProject = Project.byOwnerAndName(User.byUsername("translatr"), ctx.messages().at("brand"));
		if(brandProject != null)
			ctx.args.put("brandProjectId", brandProject.id);

		if(ctx.flash().containsKey("undo"))
		{
			String key = ctx.flash().get("undo");
			ctx.args.put("undoMessage", ((Command<?>)cache.get(key)).getMessage());
			ctx.args.put("undoCommand", key);
		}

		return delegate.call(ctx);
	}
}
