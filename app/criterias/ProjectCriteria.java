package criterias;

import forms.SearchForm;
import java.util.UUID;
import play.mvc.Http.Request;

/**
 * @author resamsel
 * @version 26 Sep 2016
 */
public class ProjectCriteria extends AbstractProjectSearchCriteria<ProjectCriteria> {

  private UUID ownerId;

  private UUID memberId;

  private String name;

  public ProjectCriteria() {
    super("project");
  }

  public UUID getOwnerId() {
    return ownerId;
  }

  private void setOwnerId(UUID ownerId) {
    this.ownerId = ownerId;
  }

  public ProjectCriteria withOwnerId(UUID ownerId) {
    setOwnerId(ownerId);
    return this;
  }

  public UUID getMemberId() {
    return memberId;
  }

  private void setMemberId(UUID memberId) {
    this.memberId = memberId;
  }

  public ProjectCriteria withMemberId(UUID memberId) {
    setMemberId(memberId);
    return this;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ProjectCriteria withName(String name) {
    setName(name);
    return this;
  }

  @Override
  protected String getCacheKeyParticle() {
    return String.format("%s:%s:%s", name, ownerId, memberId);
  }

  public static ProjectCriteria from(SearchForm form) {
    return new ProjectCriteria().with(form);
  }

  public static ProjectCriteria from(Request request) {
    return new ProjectCriteria().with(request);
  }
}
