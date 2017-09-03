package integration.controllers;

import static assertions.ResultAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static play.inject.Bindings.bind;
import static play.test.Helpers.route;
import static utils.TestFactory.requestAs;
import static utils.UserRepositoryMock.byUsername;

import com.feth.play.module.pa.providers.oauth2.google.GoogleAuthInfo;
import com.feth.play.module.pa.providers.oauth2.google.GoogleAuthUser;
import com.feth.play.module.pa.user.AuthUser;
import com.google.common.collect.ImmutableMap;
import controllers.Profiles;
import controllers.routes;
import criterias.HasNextPagedList;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import models.AccessToken;
import models.User;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.api.inject.Binding;
import play.libs.Json;
import play.mvc.Result;
import repositories.AccessTokenRepository;
import repositories.UserRepository;

public class ProfilesTest extends ControllerTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProfilesTest.class);

  private UserRepository userRepository;
  private AccessTokenRepository accessTokenRepository;

  private User johnSmith;

  @Test
  public void testProfileDenied() {
    assertAccessDenied(routes.Profiles.profile(), "profile denied");
  }

  @Test
  public void testProfile() {
    Result result = route(app, requestAs(johnSmith).uri(routes.Profiles.profile().url()));

    assertThat(result)
        .as("profile")
        .statusIsEqualTo(Profiles.SEE_OTHER)
        .headerIsEqualTo("location", johnSmith.profileRoute().url());
  }

  @Test
  public void testEditDenied() {
    assertAccessDenied(routes.Profiles.edit(), "profile edit denied");
  }

  @Test
  public void testEdit() {
    Result result = route(app, requestAs(johnSmith).uri(routes.Profiles.edit().url()));

    assertThat(result)
        .as("profile edit")
        .statusIsEqualTo(Profiles.OK)
        .contentTypeIsEqualTo("text/html")
        .charsetIsEqualTo("utf-8")
        .contentContains(johnSmith.email, mat)
        .contentContains(johnSmith.name, mat)
        .contentContains(johnSmith.username, mat);
  }

  @Test
  public void testDoEditDenied() {
    assertAccessDenied(routes.Profiles.doEdit(), "profile do edit denied");
  }

  @Test
  public void testDoEdit() {
    Result result = route(app,
        requestAs(johnSmith)
            .method("POST")
            .bodyForm(ImmutableMap.of(
                "id", johnSmith.id.toString(),
                "name", johnSmith.name,
                "username", johnSmith.username,
                "email", johnSmith.email
            ))
            .uri(routes.Profiles.doEdit().url())
    );

    assertThat(result)
        .as("profile do edit")
        .statusIsEqualTo(Profiles.SEE_OTHER)
        .headerIsEqualTo("location", johnSmith.profileRoute().url());
  }

  @Test
  public void testProjectsDenied() {
    assertAccessDenied(routes.Profiles.projects(), "projects denied");
  }

  @Test
  public void testProjects() {
    Result result = route(app, requestAs(johnSmith).uri(routes.Profiles.projects().url()));

    assertThat(result)
        .as("projects")
        .statusIsEqualTo(Profiles.SEE_OTHER)
        .headerIsEqualTo("location", johnSmith.projectsRoute().url());
  }

  @Test
  public void testActivityDenied() {
    assertAccessDenied(routes.Profiles.activity(), "activity denied");
  }

  @Test
  public void testActivity() {
    Result result = route(app, requestAs(johnSmith).uri(routes.Profiles.activity().url()));

    assertThat(result)
        .as("activity")
        .statusIsEqualTo(Profiles.SEE_OTHER)
        .headerIsEqualTo("location", johnSmith.activityRoute().url());
  }

  @Test
  public void testAccessTokensDenied() {
    assertAccessDenied(routes.Profiles.accessTokens(), "access tokens denied");
  }

  @Test
  public void testAccessTokens() {
    Result result =
        route(app, requestAs(johnSmith).uri(routes.Profiles.accessTokens().url()));

    assertThat(result)
        .as("access tokens")
        .statusIsEqualTo(Profiles.SEE_OTHER)
        .headerIsEqualTo("location", johnSmith.accessTokensRoute().url());
  }

  @Test
  public void testAccessTokensCreateDenied() {
    assertAccessDenied(routes.Profiles.accessTokenCreate(), "access token create denied");
  }

  @Test
  public void testAccessTokenCreate() {
    Result result =
        route(app, requestAs(johnSmith).uri(routes.Profiles.accessTokenCreate().url()));

    assertThat(result)
        .as("access token create")
        .statusIsEqualTo(Profiles.OK)
        .contentTypeIsEqualTo("text/html")
        .charsetIsEqualTo("utf-8");
  }

  @Test
  public void testDoAccessTokenCreateDenied() {
    assertAccessDenied(routes.Profiles.doAccessTokenCreate(), "access token do create denied");
  }

  @Test
  public void testDoAccessTokenCreate() {
    Result result = route(app,
        requestAs(johnSmith)
            .method("POST")
            .bodyForm(ImmutableMap.of(
                "key", UUID.randomUUID().toString(),
                "name", "Access Token Name"
            ))
            .uri(routes.Profiles.doAccessTokenCreate().url())
    );

    AccessToken accessToken = accessTokenRepository
        .byUserAndName(johnSmith.id, "Access Token Name");

    assertThat(result)
        .as("access token do create")
        .statusIsEqualTo(Profiles.SEE_OTHER)
        .headerIsEqualTo("location", accessToken.editRoute().url());
  }

  @Test
  public void testLinkedAccountsDenied() {
    assertAccessDenied(routes.Profiles.linkedAccounts(), "linked accounts denied");
  }

  @Test
  public void testLinkedAccounts() {
    Result result =
        route(app, requestAs(johnSmith).uri(routes.Profiles.linkedAccounts().url()));

    assertThat(result)
        .as("linked accounts")
        .statusIsEqualTo(Profiles.SEE_OTHER)
        .headerIsEqualTo("location", johnSmith.linkedAccountsRoute().url());
  }

  @Test
  public void testResetNotificationsDenied() {
    assertAccessDenied(routes.Profiles.resetNotifications(), "reset notifications denied");
  }

  @Test
  public void testResetNotifications() {
    Result result =
        route(app, requestAs(johnSmith).uri(routes.Profiles.resetNotifications().url()));

    assertThat(result)
        .as("reset notifications")
        .statusIsEqualTo(Profiles.OK)
        .contentIsEqualTo("", mat);
  }

  @Test
  public void testAskLinkDenied() {
    assertAccessDenied(routes.Profiles.askLink(), "ask link denied");
  }

  @Test
  public void testAskLink() {
    Result result = route(app, requestAs(johnSmith).uri(routes.Profiles.askLink().url()));

    assertThat(result)
        .as("ask link missing session info")
        .statusIsEqualTo(Profiles.SEE_OTHER)
        .headerIsEqualTo("location", routes.Application.index().url());

    String sessionId = UUID.randomUUID().toString();
    AuthUser authUser = new GoogleAuthUser(
        Json.newObject()
            .put("id", "12345"),
        new GoogleAuthInfo(
            Json.newObject()
                .put("token_type", "bearer")
                .put("id_token", "12345")
        ),
        ""
    );
    when(cache.get(eq(sessionId + "_null"))).thenReturn(authUser);

    result = route(app,
        requestAs(johnSmith).session("pa.s.id", sessionId).uri(routes.Profiles.askLink().url()));

    assertThat(result)
        .as("ask link")
        .statusIsEqualTo(Profiles.OK);
  }

  @Test
  public void testDoLinkDenied() {
    assertAccessDenied(routes.Profiles.doLink(), "do link denied");
  }

  @Test
  public void testAskMergeDenied() {
    assertAccessDenied(routes.Profiles.askMerge(), "ask merge denied");
  }

  @Test
  public void testDoMergeDenied() {
    assertAccessDenied(routes.Profiles.doMerge(), "do merge denied");
  }

  @Override
  protected Binding<?>[] createBindings() {
    johnSmith = byUsername("johnsmith");

    userRepository = mock(UserRepository.class,
        withSettings().invocationListeners(i -> LOGGER.debug("{}", i.getInvocation()))
    );
    accessTokenRepository = mock(AccessTokenRepository.class,
        withSettings().invocationListeners(i -> LOGGER.debug("{}", i.getInvocation()))
    );

    when(userRepository.findByAuthUserIdentity(any())).thenReturn(johnSmith);
    when(userRepository.update(any())).then(this::persist);
    when(accessTokenRepository.save((AccessToken) any())).then(this::persistAccessToken);

    return ArrayUtils.addAll(
        super.createBindings(),
        bind(UserRepository.class).toInstance(userRepository),
        bind(AccessTokenRepository.class).toInstance(accessTokenRepository)
    );
  }

  private User updateUserMocks(User t) {
    LOGGER.debug("updateUserMocks({})", t);

    if (t.id == null) {
      t.id = UUID.randomUUID();
    }
    t.linkedAccounts = new ArrayList<>(t.linkedAccounts);

    when(userRepository.byId(eq(t.id), any())).thenReturn(t);
    when(userRepository.findBy(any())).thenReturn(HasNextPagedList.create(t));

    return t;
  }

  private AccessToken updateAccessTokenMocks(AccessToken t) {
    LOGGER.debug("updateAccessTokenMocks({})", t);

    if (t.id == null) {
      t.id = ThreadLocalRandom.current().nextLong();
    }

    when(accessTokenRepository.byId(eq(t.id), any())).thenReturn(t);
    when(accessTokenRepository.byUserAndName(eq(t.user.id), eq(t.name))).thenReturn(t);

    return t;
  }

  private User persist(InvocationOnMock a) {
    return updateUserMocks(a.getArgument(0));
  }

  private AccessToken persistAccessToken(InvocationOnMock a) {
    return updateAccessTokenMocks(a.getArgument(0));
  }
}
