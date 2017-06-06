package forms;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import models.AccessToken;
import models.Scope;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints;

/**
 *
 * @author resamsel
 * @version 2 Sep 2016
 */
public class AccessTokenForm extends SearchForm {
  @Constraints.MaxLength(AccessToken.NAME_LENGTH)
  // @AccessTokenByUserAndName
  private String name;

  private String key;

  private List<Scope> scopes = new ArrayList<>();

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getKey() {
    return key;
  }

  public Boolean getScopeUserRead() {
    return scopes.contains(Scope.UserRead);
  }

  public void setScopeUserRead(Boolean scopeUserRead) {
    scopes.add(Scope.UserRead);
  }

  public Boolean getScopeUserWrite() {
    return scopes.contains(Scope.UserWrite);
  }

  public void setScopeUserWrite(Boolean scopeUserWrite) {
    scopes.add(Scope.UserWrite);
  }

  public Boolean getScopeProjectRead() {
    return scopes.contains(Scope.ProjectRead);
  }

  public void setScopeProjectRead(Boolean scopeProjectRead) {
    scopes.add(Scope.ProjectRead);
  }

  public Boolean getScopeProjectWrite() {
    return scopes.contains(Scope.ProjectWrite);
  }

  public void setScopeProjectWrite(Boolean scopeProjectWrite) {
    scopes.add(Scope.ProjectWrite);
  }

  public Boolean getScopeLocaleRead() {
    return scopes.contains(Scope.LocaleRead);
  }

  public void setScopeLocaleRead(Boolean scopeLocaleRead) {
    scopes.add(Scope.LocaleRead);
  }

  public Boolean getScopeLocaleWrite() {
    return scopes.contains(Scope.LocaleWrite);
  }

  public void setScopeLocaleWrite(Boolean scopeLocaleWrite) {
    scopes.add(Scope.LocaleWrite);
  }

  public Boolean getScopeKeyRead() {
    return scopes.contains(Scope.KeyRead);
  }

  public void setScopeKeyRead(Boolean scopeKeyRead) {
    scopes.add(Scope.KeyRead);
  }

  public Boolean getScopeKeyWrite() {
    return scopes.contains(Scope.KeyWrite);
  }

  public void setScopeKeyWrite(Boolean scopeKeyWrite) {
    scopes.add(Scope.KeyWrite);
  }

  public Boolean getScopeMessageRead() {
    return scopes.contains(Scope.MessageRead);
  }

  public void setScopeMessageRead(Boolean scopeMessageRead) {
    scopes.add(Scope.MessageRead);
  }

  public Boolean getScopeMessageWrite() {
    return scopes.contains(Scope.MessageWrite);
  }

  public void setScopeMessageWrite(Boolean scopeMessageWrite) {
    scopes.add(Scope.MessageWrite);
  }

  public Boolean getScopeNotificationRead() {
    return scopes.contains(Scope.NotificationRead);
  }

  public void setScopeNotificationRead(Boolean scopeNotificationRead) {
    scopes.add(Scope.NotificationRead);
  }

  public Boolean getScopeNotificationWrite() {
    return scopes.contains(Scope.NotificationWrite);
  }

  public void setScopeNotificationWrite(Boolean scopeNotificationWrite) {
    scopes.add(Scope.NotificationWrite);
  }

  /**
   * @param in
   * @return
   */
  public AccessToken fill(AccessToken in) {
    in.name = name;
    in.scope = scopes.stream().map(Scope::scope).collect(Collectors.joining(","));

    return in;
  }

  /**
   * @param project
   * @return
   */
  public static AccessTokenForm from(AccessToken in) {
    AccessTokenForm out = new AccessTokenForm();

    out.name = in.name;
    out.key = in.key;
    out.scopes = in.getScopeList();

    return out;
  }

  /**
   * @param formFactory
   * @return
   */
  public static Form<AccessTokenForm> form(FormFactory formFactory) {
    return formFactory.form(AccessTokenForm.class);
  }
}
