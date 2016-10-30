package utils;

import static org.fest.assertions.api.Assertions.assertThat;
import static utils.PermissionUtils.hasPermissionAll;
import static utils.PermissionUtils.hasPermissionAny;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import models.AccessToken;
import models.Scope;
import play.db.Database;
import play.test.WithApplication;

/**
 * @author resamsel
 * @version 27 Oct 2016
 */
public class PermissionUtilsTest extends WithApplication {
  private Database database;

  @Before
  public void createDatabase() {
    // database =
    // Databases.createFrom("com.postgresql.Driver", "jdbc:postgresql://localhost/translatr-test",
    // ImmutableMap.of("user", "translatr", "password", "translatr"));

    // Evolutions.applyEvolutions(database);
  }

  @After
  public void shutdownDatabase() {
    if (database != null)
      database.shutdown();
  }

  @Test
  public void testHasPermissionAll() {
    assertThat(hasPermissionAll(accessToken(), Scope.values())).isFalse();
    assertThat(hasPermissionAll(accessToken(Scope.ProjectRead), Scope.values())).isFalse();
    assertThat(hasPermissionAll(accessToken(Scope.ProjectRead), Scope.ProjectRead)).isTrue();
    assertThat(
        hasPermissionAll(accessToken(Scope.ProjectRead, Scope.LocaleRead), Scope.ProjectRead))
            .isTrue();
    assertThat(hasPermissionAll(accessToken(Scope.values()), Scope.values())).isTrue();
  }

  @Test
  public void testHasPermissionAny() {
    assertThat(hasPermissionAny(accessToken(), Scope.values())).isFalse();
    assertThat(
        hasPermissionAny(accessToken(Scope.ProjectRead), Scope.ProjectRead, Scope.ProjectWrite))
            .isTrue();
    assertThat(
        hasPermissionAny(accessToken(Scope.ProjectRead), Scope.LocaleRead, Scope.LocaleWrite))
            .isFalse();
    assertThat(hasPermissionAny(accessToken(Scope.values()), Scope.values())).isTrue();
  }

  /**
   * @return
   */
  private AccessToken accessToken(Scope... scopes) {
    AccessToken out = new AccessToken();
    out.scope = Arrays.asList(scopes).stream().map(s -> s.scope()).collect(Collectors.joining(","));
    return out;
  }
}
