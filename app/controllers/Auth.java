package controllers;

import org.pac4j.core.client.Client;
import org.pac4j.core.exception.http.RedirectionAction;
import org.pac4j.play.PlayWebContext;
import org.pac4j.play.http.PlayHttpActionAdapter;
import org.pac4j.play.store.PlaySessionStore;
import play.inject.Injector;
import play.mvc.Http;
import play.mvc.Result;
import services.api.AuthClientApiService;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class Auth extends AbstractController {
  private final AuthClientApiService authClientApiService;
  private final PlaySessionStore playSessionStore;

  @Inject
  public Auth(Injector injector, AuthClientApiService authClientApiService, PlaySessionStore playSessionStore) {
    super(injector);

    this.authClientApiService = authClientApiService;
    this.playSessionStore = playSessionStore;
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
          return PlayHttpActionAdapter.INSTANCE.adapt(action.get(), webContext);
        }
      }

      return notFound("Auth client " + authClientName + " not found");
    });
  }
}
