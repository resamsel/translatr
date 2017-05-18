package integration.services;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Test;

import criterias.AccessTokenCriteria;
import criterias.UserCriteria;
import models.AccessToken;
import models.User;
import services.AccessTokenService;
import tests.AbstractTest;

/**
 * @author resamsel
 * @version 28 Jan 2017
 */
public class AccessTokenServiceTest extends AbstractTest {
  private AccessTokenService accessTokenService;

  @Test
  public void find() {
    assertThat(accessTokenService.findBy(new AccessTokenCriteria()).getList()).hasSize(0);

    User user = createUser("user1", "a@b.c");
    createAccessToken(user);

    assertThat(userService.findBy(new UserCriteria()).getList()).hasSize(1);
  }

  /**
   * @param user
   * @return
   * 
   */
  private AccessToken createAccessToken(User user) {
    AccessToken out = new AccessToken();

    out.user = user;
    out.name = "accessToken1";

    accessTokenService.create(out);

    return out;
  }

  @Test
  public void create() {
    User user = createUser("user1", "user1@resamsel.com");
    AccessToken accessToken = createAccessToken(user);

    assertThat(accessToken.name).isEqualTo("accessToken1");
    assertThat(accessToken.key).isNotNull();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void injectMembers() {
    accessTokenService = app.injector().instanceOf(AccessTokenService.class);
  }
}
