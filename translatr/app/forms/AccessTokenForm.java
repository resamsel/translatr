package forms;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

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
public class AccessTokenForm {
  @Constraints.Required
  @Constraints.MaxLength(AccessToken.NAME_LENGTH)
  // @AccessTokenByUserAndName
  private String name;

  private String key;

  // private Boolean scopeUserAdmin = false;
  private Boolean scopeUserRead = false;
  private Boolean scopeUserWrite = false;
  // private Boolean scopeProjectAdmin = false;
  private Boolean scopeProjectRead = false;
  private Boolean scopeProjectWrite = false;
  // private Boolean scopeLocaleAdmin = false;
  private Boolean scopeLocaleRead = false;
  private Boolean scopeLocaleWrite = false;
  // private Boolean scopeKeyAdmin = false;
  private Boolean scopeKeyRead = false;
  private Boolean scopeKeyWrite = false;
  // private Boolean scopeMessageAdmin = false;
  private Boolean scopeMessageRead = false;
  private Boolean scopeMessageWrite = false;

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
    return scopeUserRead;
  }

  public void setScopeUserRead(Boolean scopeUserRead) {
    this.scopeUserRead = scopeUserRead;
  }

  public Boolean getScopeUserWrite() {
    return scopeUserWrite;
  }

  public void setScopeUserWrite(Boolean scopeUserWrite) {
    this.scopeUserWrite = scopeUserWrite;
  }

  public Boolean getScopeProjectRead() {
    return scopeProjectRead;
  }

  public void setScopeProjectRead(Boolean scopeProjectRead) {
    this.scopeProjectRead = scopeProjectRead;
  }

  public Boolean getScopeProjectWrite() {
    return scopeProjectWrite;
  }

  public void setScopeProjectWrite(Boolean scopeProjectWrite) {
    this.scopeProjectWrite = scopeProjectWrite;
  }

  public Boolean getScopeLocaleRead() {
    return scopeLocaleRead;
  }

  public void setScopeLocaleRead(Boolean scopeLocaleRead) {
    this.scopeLocaleRead = scopeLocaleRead;
  }

  public Boolean getScopeLocaleWrite() {
    return scopeLocaleWrite;
  }

  public void setScopeLocaleWrite(Boolean scopeLocaleWrite) {
    this.scopeLocaleWrite = scopeLocaleWrite;
  }

  public Boolean getScopeKeyRead() {
    return scopeKeyRead;
  }

  public void setScopeKeyRead(Boolean scopeKeyRead) {
    this.scopeKeyRead = scopeKeyRead;
  }

  public Boolean getScopeKeyWrite() {
    return scopeKeyWrite;
  }

  public void setScopeKeyWrite(Boolean scopeKeyWrite) {
    this.scopeKeyWrite = scopeKeyWrite;
  }

  public Boolean getScopeMessageRead() {
    return scopeMessageRead;
  }

  public void setScopeMessageRead(Boolean scopeMessageRead) {
    this.scopeMessageRead = scopeMessageRead;
  }

  public Boolean getScopeMessageWrite() {
    return scopeMessageWrite;
  }

  public void setScopeMessageWrite(Boolean scopeMessageWrite) {
    this.scopeMessageWrite = scopeMessageWrite;
  }

  /**
   * @param in
   * @return
   */
  public AccessToken fill(AccessToken in) {
    in.name = name;

    List<String> scopes = new ArrayList<>();
    // if (scopeUserAdmin)
    // scopes.add(Scope.UserAdmin.scope());
    if (scopeUserRead)
      scopes.add(Scope.UserRead.scope());
    if (scopeUserWrite)
      scopes.add(Scope.UserWrite.scope());
    // if (scopeProjectAdmin)
    // scopes.add(Scope.ProjectAdmin.scope());
    if (scopeProjectRead)
      scopes.add(Scope.ProjectRead.scope());
    if (scopeProjectWrite)
      scopes.add(Scope.ProjectWrite.scope());
    // if (scopeLocaleAdmin)
    // scopes.add(Scope.LocaleAdmin.scope());
    if (scopeLocaleRead)
      scopes.add(Scope.LocaleRead.scope());
    if (scopeLocaleWrite)
      scopes.add(Scope.LocaleWrite.scope());
    // if (scopeKeyAdmin)
    // scopes.add(Scope.KeyAdmin.scope());
    if (scopeKeyRead)
      scopes.add(Scope.KeyRead.scope());
    if (scopeKeyWrite)
      scopes.add(Scope.KeyWrite.scope());
    // if (scopeMessageAdmin)
    // scopes.add(Scope.MessageAdmin.scope());
    if (scopeMessageRead)
      scopes.add(Scope.MessageRead.scope());
    if (scopeMessageWrite)
      scopes.add(Scope.MessageWrite.scope());

    in.scope = StringUtils.join(scopes, ",");

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

    List<Scope> scopes = in.getScopeList();
    // out.scopeUserAdmin = scopes.contains(Scope.UserAdmin);
    out.scopeUserRead = scopes.contains(Scope.UserRead);
    out.scopeUserWrite = scopes.contains(Scope.UserWrite);
    // out.scopeProjectAdmin = scopes.contains(Scope.ProjectAdmin);
    out.scopeProjectRead = scopes.contains(Scope.ProjectRead);
    out.scopeProjectWrite = scopes.contains(Scope.ProjectWrite);
    // out.scopeLocaleAdmin = scopes.contains(Scope.LocaleAdmin);
    out.scopeLocaleRead = scopes.contains(Scope.LocaleRead);
    out.scopeLocaleWrite = scopes.contains(Scope.LocaleWrite);
    // out.scopeKeyAdmin = scopes.contains(Scope.KeyAdmin);
    out.scopeKeyRead = scopes.contains(Scope.KeyRead);
    out.scopeKeyWrite = scopes.contains(Scope.KeyWrite);
    // out.scopeMessageAdmin = scopes.contains(Scope.MessageAdmin);
    out.scopeMessageRead = scopes.contains(Scope.MessageRead);
    out.scopeMessageWrite = scopes.contains(Scope.MessageWrite);

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
