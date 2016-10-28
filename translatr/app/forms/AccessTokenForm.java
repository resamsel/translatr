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

  private Boolean scopeProjectAdmin = false;
  private Boolean scopeProjectRead = false;
  private Boolean scopeProjectWrite = false;
  private Boolean scopeLocaleAdmin = false;
  private Boolean scopeLocaleRead = false;
  private Boolean scopeLocaleWrite = false;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getKey() {
    return key;
  }

  public Boolean getScopeProjectAdmin() {
    return scopeProjectAdmin;
  }

  public void setScopeProjectAdmin(Boolean scopeProjectAdmin) {
    this.scopeProjectAdmin = scopeProjectAdmin;
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

  public Boolean getScopeLocaleAdmin() {
    return scopeLocaleAdmin;
  }

  public void setScopeLocaleAdmin(Boolean scopeLocaleAdmin) {
    this.scopeLocaleAdmin = scopeLocaleAdmin;
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

  /**
   * @param in
   * @return
   */
  public AccessToken fill(AccessToken in) {
    in.name = name;

    List<String> scopes = new ArrayList<>();
    if (scopeProjectAdmin)
      scopes.add(Scope.ProjectAdmin.scope());
    if (scopeProjectRead)
      scopes.add(Scope.ProjectRead.scope());
    if (scopeProjectWrite)
      scopes.add(Scope.ProjectWrite.scope());
    if (scopeLocaleAdmin)
      scopes.add(Scope.LocaleAdmin.scope());
    if (scopeLocaleRead)
      scopes.add(Scope.LocaleRead.scope());
    if (scopeLocaleWrite)
      scopes.add(Scope.LocaleWrite.scope());

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
    out.scopeProjectAdmin = scopes.contains(Scope.ProjectAdmin);
    out.scopeProjectRead = scopes.contains(Scope.ProjectRead);
    out.scopeProjectWrite = scopes.contains(Scope.ProjectWrite);
    out.scopeLocaleAdmin = scopes.contains(Scope.LocaleAdmin);
    out.scopeLocaleRead = scopes.contains(Scope.LocaleRead);
    out.scopeLocaleWrite = scopes.contains(Scope.LocaleWrite);

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
