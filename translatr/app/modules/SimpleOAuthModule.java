package modules;

import com.feth.play.module.pa.Resolver;
import com.feth.play.module.pa.providers.oauth2.facebook.FacebookAuthProvider;
import com.feth.play.module.pa.providers.oauth2.github.GithubAuthProvider;
import com.feth.play.module.pa.providers.oauth2.google.GoogleAuthProvider;

import play.api.Configuration;
import play.api.Environment;
import play.api.inject.Binding;
import play.api.inject.Module;
import scala.collection.Seq;
import services.OAuthResolver;
import services.impl.AuthenticateServiceImpl;

/**
 * Initial DI module.
 */
public class SimpleOAuthModule extends Module
{
	@Override
	public Seq<Binding<?>> bindings(Environment environment, Configuration configuration)
	{
		return seq(
			bind(Resolver.class).to(OAuthResolver.class),
			bind(AuthenticateServiceImpl.class).toSelf().eagerly(),
			bind(GoogleAuthProvider.class).toSelf().eagerly(),
			bind(GithubAuthProvider.class).toSelf().eagerly(),
			bind(FacebookAuthProvider.class).toSelf().eagerly());
	}
}
