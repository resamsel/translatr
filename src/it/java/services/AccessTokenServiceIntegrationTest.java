package services;

import criterias.AccessTokenCriteria;
import criterias.UserCriteria;
import models.AccessToken;
import models.User;
import org.junit.Test;
import play.mvc.Http;
import tests.AbstractDatabaseTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author resamsel
 * @version 28 Jan 2017
 */
public class AccessTokenServiceIntegrationTest extends AbstractDatabaseTest {

  private AccessTokenService accessTokenService;

  @Test
  public void find() {
    Http.Request request = mock(Http.Request.class);
    assertThat(accessTokenService.findBy(new AccessTokenCriteria()).getList()).hasSize(0);

    User user = createUser("user1", "a@b.c");
    createAccessToken(user, request);

    assertThat(userService.findBy(new UserCriteria()).getList()).hasSize(1);
  }

  private AccessToken createAccessToken(User user, Http.Request request) {
    AccessToken out = new AccessToken();

    out.user = user;
    out.name = "accessToken1";

    accessTokenService.create(out, request);

    return out;
  }

  @Test
  public void create() {
    Http.Request request = mock(Http.Request.class);
    User user = createUser("user1", "user1@resamsel.com");
    AccessToken accessToken = createAccessToken(user, request);

    assertThat(accessToken.name).as("AccessToken.name").isEqualTo("accessToken1");
    assertThat(accessToken.key).as("AccessToken.key").isNotNull();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void injectMembers() {
    accessTokenService = app.injector().instanceOf(AccessTokenService.class);
  }
}
