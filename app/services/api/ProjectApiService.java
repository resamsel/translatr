package services.api;

import com.avaje.ebean.PagedList;
import com.google.inject.ImplementedBy;
import criterias.ProjectCriteria;
import dto.Aggregate;
import dto.Project;
import dto.SearchResponse;
import forms.SearchForm;
import java.util.UUID;
import services.api.impl.ProjectApiServiceImpl;

/**
 * @author resamsel
 * @version 29 Jan 2017
 */
@ImplementedBy(ProjectApiServiceImpl.class)
public interface ProjectApiService extends ApiService<Project, UUID, ProjectCriteria> {

  Project byOwnerAndName(String username, String name, String... fetches);

  PagedList<Aggregate> activity(UUID userId);

  /**
   * @param projectId
   * @param searchForm
   * @return
   */
  SearchResponse search(UUID projectId, SearchForm searchForm);
}
