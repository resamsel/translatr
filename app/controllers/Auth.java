package controllers;

import org.pac4j.core.client.Client;
import org.pac4j.core.exception.http.FoundAction;
import org.pac4j.core.exception.http.RedirectionAction;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.play.PlayWebContext;
import org.pac4j.play.http.PlayHttpActionAdapter;
import org.pac4j.play.store.PlaySessionStore;
import play.inject.Injector;
import play.mvc.Http;
import play.mvc.Result;
import services.api.AuthClientApiService;
import utils.ApplicationStart;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

@Singleton
public class Auth extends AbstractController {
  private final AuthClientApiService authClientApiService;
  private final PlaySessionStore playSessionStore;

  @Inject
  public Auth(Injector injector, AuthClientApiService authClientApiService, PlaySessionStore playSessionStore, ApplicationStart applicationStart) {
    super(injector);

    this.authClientApiService = authClientApiService;
    this.playSessionStore = playSessionStore;

    applicationStart.onStart();
  }

  public CompletionStage<Result> login(Http.Request request, String authClientName) {
    return async(() -> {
      @SuppressWarnings("rawtypes")
      Optional<Client> client = authClientApiService.findClient(authClientName);

      if (client.isPresent()) {
        PlayWebContext webContext = new PlayWebContext(request, playSessionStore);
        @SuppressWarnings("unchecked")
        Optional<RedirectionAction> action = client.get().getRedirectionAction(webContext);
        if (action.isPresent()) {
          request.queryString("redirect_uri").ifPresent(redirectUri ->
                  webContext.getSessionStore().set(webContext, Pac4jConstants.REQUESTED_URL, new FoundAction(redirectUri)));
          return PlayHttpActionAdapter.INSTANCE.adapt(action.get(), webContext);
        }
      }

      return notFound("Auth client " + authClientName + " not found");
    });
  }
}
