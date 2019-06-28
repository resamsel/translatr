package security;

import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.ExecutionContextProvider;
import be.objectify.deadbolt.java.cache.HandlerCache;
import com.feth.play.module.pa.PlayAuthenticate;
import services.UserService;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MyHandlerCache implements HandlerCache
{
	private final DeadboltHandler defaultHandler;

	@Inject
	public MyHandlerCache(final PlayAuthenticate auth, final ExecutionContextProvider execContextProvider,
				final UserService userService)
	{
		this.defaultHandler = new MyDeadboltHandler(auth, execContextProvider, userService);
	}

	@Override
	public DeadboltHandler apply(final String key)
	{
		return this.defaultHandler;
	}

	@Override
	public DeadboltHandler get()
	{
		return this.defaultHandler;
	}
}
