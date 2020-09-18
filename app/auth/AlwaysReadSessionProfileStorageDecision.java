package auth;

import org.pac4j.core.client.Client;
import org.pac4j.core.client.DirectClient;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.engine.decision.ProfileStorageDecision;
import org.pac4j.core.profile.UserProfile;

import java.util.List;

public class AlwaysReadSessionProfileStorageDecision<C extends WebContext> implements ProfileStorageDecision<C> {

  /**
   * Always load from session.
   */
  @Override
  public boolean mustLoadProfilesFromSession(C context, List<Client> currentClients) {
    return true;
  }

  /**
   * Never write to session for a direct client.
   */
  @Override
  public boolean mustSaveProfileInSession(C context, List<Client> currentClients, DirectClient directClient, UserProfile profile) {
    return false;
  }
}
