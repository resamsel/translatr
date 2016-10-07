package actions;

import java.util.concurrent.CompletionStage;

import javax.inject.Inject;

import commands.Command;
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
		if(ctx.flash().containsKey("undo"))
		{
			String key = ctx.flash().get("undo");
			ctx.args.put("undoMessage", ((Command<?>)cache.get(key)).getMessage());
			ctx.args.put("undoCommand", key);
		}

		return delegate.call(ctx);
	}
}
