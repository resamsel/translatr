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

  private String ownerUsername;

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

  public String getOwnerUsername() {
    return ownerUsername;
  }

  public void setOwnerUsername(String ownerUsername) {
    this.ownerUsername = ownerUsername;
  }

  public ProjectCriteria withOwnerUsername(String ownerUsername) {
    setOwnerUsername(ownerUsername);
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
    return String.format("%s:%s:%s:%s", name, ownerId, ownerUsername, memberId);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ProjectCriteria that = (ProjectCriteria) o;

    return (ownerId != null ? ownerId.equals(that.ownerId) : that.ownerId == null) && (
        memberId != null ? memberId.equals(that.memberId) : that.memberId == null) && (name != null
        ? name.equals(that.name) : that.name == null);
  }

  @Override
  public int hashCode() {
    int result = ownerId != null ? ownerId.hashCode() : 0;
    result = 31 * result + (memberId != null ? memberId.hashCode() : 0);
    result = 31 * result + (name != null ? name.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "ProjectCriteria {" +
        "ownerId=" + ownerId +
        ", memberId=" + memberId +
        ", name='" + name + '\'' +
        ", type='" + type + '\'' +
        '}';
  }

  public static ProjectCriteria from(SearchForm form) {
    return new ProjectCriteria().with(form);
  }

  public static ProjectCriteria from(Request request) {
    return new ProjectCriteria()
        .with(request)
        .withOwnerUsername(request.getQueryString("owner"));
  }
}
