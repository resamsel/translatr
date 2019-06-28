package modules;

import com.feth.play.module.pa.Resolver;
import com.feth.play.module.pa.providers.oauth1.twitter.TwitterAuthProvider;
import com.feth.play.module.pa.providers.oauth2.facebook.FacebookAuthProvider;
import com.feth.play.module.pa.providers.oauth2.github.GithubAuthProvider;
import com.feth.play.module.pa.providers.oauth2.google.GoogleAuthProvider;
import com.feth.play.module.pa.providers.oauth2.keycloak.KeycloakAuthProvider;
import org.apache.commons.lang3.StringUtils;
import play.api.Configuration;
import play.api.Environment;
import play.api.inject.Binding;
import play.api.inject.Module;
import scala.Option;
import scala.collection.Seq;
import services.OAuthResolver;
import services.impl.AuthenticateServiceImpl;
import utils.ConfigKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Initial DI module.
 */
public class SimpleOAuthModule extends Module {
  @Override
  public Seq<Binding<?>> bindings(Environment environment, Configuration configuration) {
    List<Binding<?>> bindings = new ArrayList<>();

    bindings.add(bind(Resolver.class).to(OAuthResolver.class));
    bindings.add(bind(AuthenticateServiceImpl.class).toSelf().eagerly());

    Option<String> providersOption =
        configuration.getString(ConfigKey.AuthProviders.key(), Option.empty());
    if (providersOption.nonEmpty()) {
      List<String> providers = Arrays.asList(StringUtils.split(providersOption.get(), ","));
      if (providers.contains(KeycloakAuthProvider.PROVIDER_KEY))
        bindings.add(bind(KeycloakAuthProvider.class).toSelf().eagerly());
      if (providers.contains(GoogleAuthProvider.PROVIDER_KEY))
        bindings.add(bind(GoogleAuthProvider.class).toSelf().eagerly());
      if (providers.contains(GithubAuthProvider.PROVIDER_KEY))
        bindings.add(bind(GithubAuthProvider.class).toSelf().eagerly());
      if (providers.contains(FacebookAuthProvider.PROVIDER_KEY))
        bindings.add(bind(FacebookAuthProvider.class).toSelf().eagerly());
      if (providers.contains(TwitterAuthProvider.PROVIDER_KEY))
        bindings.add(bind(TwitterAuthProvider.class).toSelf().eagerly());
    }

    return seq(bindings.toArray(new Binding[bindings.size()]));
  }
}
