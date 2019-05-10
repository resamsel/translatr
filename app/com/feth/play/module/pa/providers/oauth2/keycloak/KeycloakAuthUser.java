package com.feth.play.module.pa.providers.oauth2.keycloak;

import com.fasterxml.jackson.databind.JsonNode;
import com.feth.play.module.pa.providers.oauth2.BasicOAuth2AuthUser;
import com.feth.play.module.pa.providers.oauth2.OAuth2AuthInfo;
import com.feth.play.module.pa.user.BasicIdentity;
import com.feth.play.module.pa.user.FirstLastNameIdentity;
import com.feth.play.module.pa.user.PreferredUsernameIdentity;
import com.feth.play.module.pa.user.UserRoleIdentity;
import models.UserRole;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author resamsel
 * @version 4 Jan 2017
 */
public class KeycloakAuthUser extends BasicOAuth2AuthUser
    implements BasicIdentity, FirstLastNameIdentity, UserRoleIdentity, PreferredUsernameIdentity {
  private static final long serialVersionUID = -3369519859589372425L;

  private static class Constants {
    public static final String ID = "sub"; // "",
    public static final String EMAIL = "email"; // "fred.example@gmail.com",
    public static final String NAME = "name";
    public static final String FIRST_NAME = "given_name"; // "Fred",
    public static final String LAST_NAME = "family_name"; // "Example",
    public static final String PREFERRED_USERNAME = "preferred_username";
    public static final String GROUPS = "groups";
  }

  private String email;
  private String preferredUsername;
  private UserRole userRole = UserRole.User;
  private String name;
  private String firstName;
  private String lastName;

  /**
   * @param n
   * @param info
   * @param state
   */
  KeycloakAuthUser(JsonNode n, OAuth2AuthInfo info, String state) {
    super(n.get(Constants.ID).asText(), info, state);

    if (n.has(Constants.EMAIL)) {
      this.email = n.get(Constants.EMAIL).asText();
    }

    if (n.has(Constants.NAME)) {
      this.name = n.get(Constants.NAME).asText();
    }

    if (n.has(Constants.FIRST_NAME)) {
      this.firstName = n.get(Constants.FIRST_NAME).asText();
    }
    if (n.has(Constants.LAST_NAME)) {
      this.lastName = n.get(Constants.LAST_NAME).asText();
    }

    if (n.has(Constants.PREFERRED_USERNAME)) {
      this.preferredUsername = n.get(Constants.PREFERRED_USERNAME).asText();
    }

    if (n.has(Constants.GROUPS) && n.get(Constants.GROUPS).isArray()) {
      Stream<JsonNode> stream = StreamSupport.stream(Spliterators.spliteratorUnknownSize(
          n.get(Constants.GROUPS).iterator(), Spliterator.ORDERED), false);
      if (stream.map(JsonNode::asText).anyMatch("admin"::equalsIgnoreCase)) {
        this.userRole = UserRole.Admin;
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getEmail() {
    return email;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getProvider() {
    return KeycloakAuthProvider.PROVIDER_KEY;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getName() {
    return name;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getFirstName() {
    return firstName;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getLastName() {
    return lastName;
  }

  @Override
  public UserRole getUserRole() {
    return userRole;
  }

  @Override
  public String getPreferredUsername() {
    return preferredUsername;
  }
}
