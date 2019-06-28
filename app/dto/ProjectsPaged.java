package dto;

import com.avaje.ebean.PagedList;

import java.util.function.Function;

/**
 * @author resamsel
 * @version 10 Feb 2017
 */
public class ProjectsPaged extends DtoPagedList<models.Project, Project> {
  private static final long serialVersionUID = 3322780965933160813L;

  /**
   * @param delegate
   * @param mapper
   * 
   */
  public ProjectsPaged(PagedList<models.Project> delegate,
      Function<models.Project, Project> mapper) {
    super(delegate, mapper);
  }
}
