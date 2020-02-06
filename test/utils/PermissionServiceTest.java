package utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.stream.Collectors;
import models.AccessToken;
import models.Scope;
import org.junit.Test;
import services.PermissionService;
import tests.AbstractTest;

/**
 * @author resamsel
 * @version 27 Oct 2016
 */
public class PermissionServiceTest extends AbstractTest {

  private PermissionService permissionService;

  @Test
  public void testHasPermissionAll() {
    assertThat(permissionService.hasPermissionAll(accessToken(), Scope.values())).isFalse();
    assertThat(permissionService.hasPermissionAll(accessToken(Scope.ProjectRead), Scope.values()))
        .isFalse();
    assertThat(
        permissionService.hasPermissionAll(accessToken(Scope.ProjectRead), Scope.ProjectRead))
        .isTrue();
    assertThat(
        permissionService
            .hasPermissionAll(accessToken(Scope.ProjectRead, Scope.LocaleRead), Scope.ProjectRead))
        .isTrue();
    assertThat(permissionService.hasPermissionAll(accessToken(Scope.values()), Scope.values()))
        .isTrue();
  }

  @Test
  public void testHasPermissionAny() {
    assertThat(permissionService.hasPermissionAny(accessToken(), Scope.values())).isFalse();
    assertThat(
        permissionService.hasPermissionAny(accessToken(Scope.ProjectRead), Scope.ProjectRead,
            Scope.ProjectWrite))
        .isTrue();
    assertThat(
        permissionService
            .hasPermissionAny(accessToken(Scope.ProjectRead), Scope.LocaleRead, Scope.LocaleWrite))
        .isFalse();
    assertThat(permissionService.hasPermissionAny(accessToken(Scope.values()), Scope.values()))
        .isTrue();
  }

  private AccessToken accessToken(Scope... scopes) {
    AccessToken out = new AccessToken();
    out.scope = Arrays.stream(scopes).map(Scope::scope).collect(Collectors.joining(","));
    return out;
  }

  @Override
  protected void injectMembers() {
    permissionService = app.injector().instanceOf(PermissionService.class);
  }
}
