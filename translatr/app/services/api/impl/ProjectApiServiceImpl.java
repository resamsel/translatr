package services.api.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.avaje.ebean.PagedList;
import com.fasterxml.jackson.databind.JsonNode;

import controllers.Keys;
import controllers.Locales;
import controllers.routes;
import criterias.KeyCriteria;
import criterias.LocaleCriteria;
import criterias.ProjectCriteria;
import dto.SearchResponse;
import dto.Suggestion;
import forms.SearchForm;
import models.Key;
import models.Locale;
import models.Project;
import models.ProjectRole;
import models.Scope;
import models.Suggestable;
import models.Suggestable.Data;
import play.Configuration;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.Http.Context;
import services.ProjectService;
import services.api.ProjectApiService;
import utils.PermissionUtils;

/**
 * @author resamsel
 * @version 29 Jan 2017
 */
@Singleton
public class ProjectApiServiceImpl extends
    AbstractApiService<Project, UUID, ProjectCriteria, dto.Project> implements ProjectApiService {
  private final Configuration configuration;

  /**
   * @param projectService
   */
  @Inject
  protected ProjectApiServiceImpl(Configuration configuration, ProjectService projectService) {
    super(projectService, dto.Project.class, dto.Project::from, new Scope[] {Scope.ProjectRead},
        new Scope[] {Scope.ProjectWrite});
    this.configuration = configuration;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SearchResponse search(UUID projectId, SearchForm search) {
    checkPermissionAll("Access token not allowed", readScopes);

    Messages messages = Context.current().messages();

    dto.Project project = get(projectId);

    search.setLimit(configuration.getInt("translatr.search.autocomplete.limit", 3));

    List<Suggestable> suggestions = new ArrayList<>();

    if (PermissionUtils.hasPermissionAll(Scope.KeyRead)) {
      PagedList<? extends Suggestable> keys = Key.findBy(
          KeyCriteria.from(search).withProjectId(project.id).withOrder("whenUpdated desc"));

      search.pager(keys);

      if (!keys.getList().isEmpty())
        suggestions.addAll(keys.getList());
      if (search.hasMore)
        suggestions
            .add(Suggestable.DefaultSuggestable.from(messages.at("key.search", search.search),
                Data.from(Key.class, null, "???",
                    search.urlWithOffset(
                        routes.Projects.keys(project.id, Keys.DEFAULT_SEARCH, Keys.DEFAULT_ORDER,
                            Keys.DEFAULT_LIMIT, Keys.DEFAULT_OFFSET),
                        Keys.DEFAULT_LIMIT, Keys.DEFAULT_OFFSET))));

      if (PermissionUtils.hasPermissionAny(project.id, ProjectRole.Owner, ProjectRole.Manager,
          ProjectRole.Developer) && PermissionUtils.hasPermissionAll(Scope.KeyWrite))
        suggestions.add(Suggestable.DefaultSuggestable
            .from(messages.at("key.create", search.search), Data.from(Key.class, null, "+++",
                routes.Keys.createImmediately(project.id, search.search).url())));
    }

    if (PermissionUtils.hasPermissionAll(Scope.LocaleRead)) {
      PagedList<? extends Suggestable> locales = Locale.findBy(new LocaleCriteria()
          .withProjectId(project.id).withSearch(search.search).withOrder("whenUpdated desc"));

      search.pager(locales);
      if (!locales.getList().isEmpty())
        suggestions.addAll(locales.getList());
      if (search.hasMore)
        suggestions
            .add(Suggestable.DefaultSuggestable.from(messages.at("locale.search", search.search),
                Data.from(Locale.class, null, "???",
                    search.urlWithOffset(
                        routes.Projects.locales(project.id, Locales.DEFAULT_SEARCH,
                            Locales.DEFAULT_ORDER, Locales.DEFAULT_LIMIT, Locales.DEFAULT_OFFSET),
                        Locales.DEFAULT_LIMIT, Locales.DEFAULT_OFFSET))));

      if (PermissionUtils.hasPermissionAny(project.id, ProjectRole.Owner, ProjectRole.Translator)
          && PermissionUtils.hasPermissionAll(Scope.LocaleWrite))
        suggestions.add(Suggestable.DefaultSuggestable
            .from(messages.at("locale.create", search.search), Data.from(Locale.class, null, "+++",
                routes.Locales.createImmediately(project.id, search.search).url())));
    }

    return SearchResponse.from(Suggestion.from(suggestions));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Project toModel(JsonNode json) {
    return Json.fromJson(json, dto.Project.class).toModel();
  }
}
