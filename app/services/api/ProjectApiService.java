package services.api;

import io.ebean.PagedList;
import com.google.inject.ImplementedBy;
import criterias.ProjectCriteria;
import dto.Aggregate;
import dto.Project;
import dto.SearchResponse;
import forms.SearchForm;
import play.mvc.Http;
import services.api.impl.ProjectApiServiceImpl;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * @author resamsel
 * @version 29 Jan 2017
 */
@ImplementedBy(ProjectApiServiceImpl.class)
public interface ProjectApiService extends ApiService<Project, UUID, ProjectCriteria> {

  Project byOwnerAndName(Http.Request request, String username, String name, Consumer<models.Project> validator, String... fetches);

  PagedList<Aggregate> activity(Http.Request request, UUID userId);

  SearchResponse search(Http.Request request, UUID projectId, SearchForm searchForm);
}
