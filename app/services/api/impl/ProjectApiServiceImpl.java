package services.api.impl;

import com.avaje.ebean.PagedList;
import com.fasterxml.jackson.databind.JsonNode;
import controllers.Keys;
import controllers.Locales;
import controllers.routes;
import criterias.*;
import dto.DtoPagedList;
import dto.SearchResponse;
import dto.Suggestion;
import forms.SearchForm;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;

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
import services.*;
import services.api.ProjectApiService;

/**
 * @author resamsel
 * @version 29 Jan 2017
 */
@Singleton
public class ProjectApiServiceImpl extends
    AbstractApiService<Project, UUID, ProjectCriteria, ProjectService, dto.Project> implements
    ProjectApiService {

  private final Configuration configuration;
  private final LocaleService localeService;
  private final KeyService keyService;
  private final LogEntryService logEntryService;

  @Inject
  protected ProjectApiServiceImpl(
      Configuration configuration, ProjectService projectService,
      LocaleService localeService, KeyService keyService, LogEntryService logEntryService,
      PermissionService permissionService) {
    super(projectService, dto.Project.class, dto.Project::from,
        new Scope[]{Scope.ProjectRead},
        new Scope[]{Scope.ProjectWrite},
        permissionService);

    this.configuration = configuration;
    this.localeService = localeService;
    this.keyService = keyService;
    this.logEntryService = logEntryService;
  }

  @Override
  public dto.Project byOwnerAndName(String username, String name, String... fetches) {
    return Optional.ofNullable(service.byOwnerAndName(username, name, fetches))
            .map(dtoMapper)
            .orElse(null);
  }

  @Override
  public PagedList<dto.Aggregate> activity(UUID id) {
    return new DtoPagedList<>(
        logEntryService.getAggregates(new LogEntryCriteria().withProjectId(id)),
        dto.Aggregate::from);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SearchResponse search(UUID projectId, SearchForm search) {
    permissionService.checkPermissionAll("Access token not allowed", readScopes);

    Messages messages = Context.current().messages();

    dto.Project project = get(projectId);

    search.setLimit(configuration.getInt("translatr.search.autocomplete.limit", 3));

    List<Suggestable> suggestions = new ArrayList<>();

    if (permissionService.hasPermissionAll(Scope.KeyRead)) {
      PagedList<? extends Suggestable> keys = keyService
          .findBy(KeyCriteria.from(search).withProjectId(project.id).withOrder("whenUpdated desc"));

      search.pager(keys);

      if (!keys.getList().isEmpty()) {
        suggestions.addAll(keys.getList());
      }
      if (search.hasMore) {
        suggestions.add(Suggestable.DefaultSuggestable.from(
            messages.at("key.search", search.search),
            Data.from(Key.class, null, "???",
                search.urlWithOffset(
                    routes.Projects.keysBy(project.ownerUsername, project.name, Keys.DEFAULT_SEARCH,
                        Keys.DEFAULT_ORDER, Keys.DEFAULT_LIMIT, Keys.DEFAULT_OFFSET),
                    Keys.DEFAULT_LIMIT, Keys.DEFAULT_OFFSET))));
      }

      if (permissionService.hasPermissionAny(project.id, ProjectRole.Owner, ProjectRole.Manager,
          ProjectRole.Developer) && permissionService.hasPermissionAll(Scope.KeyWrite)) {
        suggestions
            .add(
                Suggestable.DefaultSuggestable.from(messages.at("key.create", search.search),
                    Data.from(Key.class, null, "+++",
                        Keys.createImmediatelyRoute(project, search.search, search.search,
                            Keys.DEFAULT_ORDER, Keys.DEFAULT_LIMIT, Keys.DEFAULT_OFFSET)
                            .url())));
      }
    }

    if (permissionService.hasPermissionAll(Scope.LocaleRead)) {
      PagedList<? extends Suggestable> locales = localeService.findBy(new LocaleCriteria()
          .withProjectId(project.id).withSearch(search.search).withOrder("whenUpdated desc"));

      search.pager(locales);
      if (!locales.getList().isEmpty()) {
        suggestions.addAll(locales.getList());
      }
      if (search.hasMore) {
        suggestions.add(Suggestable.DefaultSuggestable.from(
            messages.at("locale.search", search.search),
            Data.from(Locale.class, null, "???",
                search.urlWithOffset(routes.Projects.localesBy(project.ownerUsername, project.name,
                    Locales.DEFAULT_SEARCH, Locales.DEFAULT_ORDER, Locales.DEFAULT_LIMIT,
                    Locales.DEFAULT_OFFSET), Locales.DEFAULT_LIMIT, Locales.DEFAULT_OFFSET))));
      }

      if (permissionService.hasPermissionAny(project.id, ProjectRole.Owner, ProjectRole.Translator)
          && permissionService.hasPermissionAll(Scope.LocaleWrite)) {
        suggestions
            .add(
                Suggestable.DefaultSuggestable
                    .from(
                        messages.at("locale.create", search.search), Data
                            .from(Locale.class, null, "+++",
                                Locales.createImmediatelyRoute(project, search.search,
                                    search.search, search.order, search.limit, search.offset)
                                    .url())));
      }
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
