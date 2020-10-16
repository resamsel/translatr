package utils;

import com.typesafe.config.Config;
import models.AccessToken;
import models.Scope;
import models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.AccessTokenRepository;
import repositories.UserRepository;

import javax.inject.Inject;
import javax.inject.Singleton;

import static utils.ConfigKey.AdminAccessToken;

@Singleton
public class ApplicationStart {
  private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationStart.class);
  private static final String ADMIN_USERNAME = "translatr";

  private final Config config;
  private final UserRepository userRepository;
  private final AccessTokenRepository accessTokenRepository;

  @Inject
  public ApplicationStart(
          Config config,
          UserRepository userRepository,
          AccessTokenRepository accessTokenRepository) {
    this.config = config;
    this.userRepository = userRepository;
    this.accessTokenRepository = accessTokenRepository;
  }

  public void onStart() {
    if (AdminAccessToken.existsIn(config)) {
      LOGGER.debug("Checking whether or not an access token for ''{}'' needs to be created", ADMIN_USERNAME);

      User user = userRepository.byUsername(ADMIN_USERNAME, UserRepository.FETCH_ACCESS_TOKENS);
      if (user != null && user.accessTokens.size() == 0) {
        LOGGER.debug("''{}'' has no access token, creating one", ADMIN_USERNAME);

        AccessToken accessToken = accessTokenRepository.create(
                new AccessToken()
                        .withUser(user)
                        .withName("Admin Access Token")
                        .withKey(AdminAccessToken.get(config))
                        .withScope(Scope.values()));

        LOGGER.warn("Access token ''{}'' has been created for admin user ''{}'' with all scopes",
                accessToken.name, ADMIN_USERNAME);
      }
    }
  }
}
