package utils;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import models.AccessToken;
import models.Scope;
import play.mvc.Http.Context;

/**
 * @author resamsel
 * @version 21 Oct 2016
 */
public class PermissionUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(PermissionUtils.class);

  public static boolean hasPermissionAll(Scope... scopes) {
    return hasPermissionAll((AccessToken) Context.current().args.get("accessToken"), scopes);
  }

  public static boolean hasPermissionAll(AccessToken accessToken, Scope... scopes) {
    LOGGER.debug("Scopes of access token: {}, needed: {}",
        accessToken != null ? accessToken.getScopeList() : "-", scopes);

    if (accessToken == null)
      return false;

    // TODO: allow admin scopes also
    // !PermissionUtils.hasPermissionAny(Scope.ProjectRead, Scope.ProjectAdmin)
    // || !PermissionUtils.hasPermissionAny(Scope.LocaleRead, Scope.LocaleAdmin))

    return accessToken.getScopeList().containsAll(Arrays.asList(scopes));
  }

  public static boolean hasPermissionAny(Scope... scopes) {
    return hasPermissionAny((AccessToken) Context.current().args.get("accessToken"), scopes);
  }

  public static boolean hasPermissionAny(AccessToken accessToken, Scope... scopes) {
    LOGGER.debug("Scopes of access token: {}, needed: {}",
        accessToken != null ? accessToken.getScopeList() : "-", scopes);

    if (accessToken == null)
      return false;

    List<Scope> scopeList = accessToken.getScopeList();
    scopeList.retainAll(Arrays.asList(scopes));

    return !scopeList.isEmpty();
  }
}
