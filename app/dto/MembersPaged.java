package dto;

import io.ebean.PagedList;

import java.util.function.Function;

/**
 * @author resamsel
 * @version 10 Feb 2017
 */
public class MembersPaged extends DtoPagedList<models.ProjectUser, ProjectUser> {
  private static final long serialVersionUID = -8200869140583550899L;

  /**
   * @param delegate
   * @param mapper
   */
  public MembersPaged(PagedList<models.ProjectUser> delegate, Function<models.ProjectUser, ProjectUser> mapper) {
    super(delegate, mapper);
  }
}
