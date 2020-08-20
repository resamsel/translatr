package services.api.impl;

import com.typesafe.config.Config;
import criterias.KeyCriteria;
import criterias.LocaleCriteria;
import criterias.LogEntryCriteria;
import criterias.ProjectCriteria;
import dto.DtoPagedList;
import dto.NotFoundException;
import dto.SearchResponse;
import forms.SearchForm;
import io.ebean.PagedList;
import mappers.AggregateMapper;
import mappers.ProjectMapper;
import mappers.SuggestionMapper;
import models.Key;
import models.Locale;
import models.Project;
import models.ProjectRole;
import models.Scope;
import models.Suggestable;
import models.Suggestable.Data;
import models.User;
import models.UserRole;
import play.i18n.Messages;
import play.mvc.Http.Context;
import services.AuthProvider;
import services.KeyService;
import services.LocaleService;
import services.LogEntryService;
import services.PermissionService;
import services.ProjectService;
import services.api.ProjectApiService;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static utils.FunctionalUtils.peek;

/**
 * @author resamsel
 * @version 29 Jan 2017
 */
@Singleton
public class ProjectApiServiceImpl extends
        AbstractApiService<Project, UUID, ProjectCriteria, ProjectService, dto.Project> implements
        ProjectApiService {

  private final Config configuration;
  private final LocaleService localeService;
  private final KeyService keyService;
  private final LogEntryService logEntryService;
  private final AuthProvider authProvider;

  @Inject
  protected ProjectApiServiceImpl(
          Config configuration, ProjectService projectService,
          LocaleService localeService, KeyService keyService, LogEntryService logEntryService,
          PermissionService permissionService, AuthProvider authProvider, Validator validator) {
    super(projectService, dto.Project.class, ProjectMapper::toDto,
            new Scope[]{Scope.ProjectRead},
            new Scope[]{Scope.ProjectWrite},
            permissionService,
            validator);

    this.configuration = configuration;
    this.localeService = localeService;
    this.keyService = keyService;
    this.logEntryService = logEntryService;
    this.authProvider = authProvider;
  }

  @Override
  public PagedList<dto.Project> find(ProjectCriteria criteria) {
    User loggedInUser = authProvider.loggedInUser();
    if (loggedInUser != null && loggedInUser.role != UserRole.Admin) {
      criteria.withMemberId(loggedInUser.id);
    }

    return super.find(criteria);
  }

  @Override
  public dto.Project byOwnerAndName(String username, String name, @Nonnull Consumer<Project> validator, String... fetches) {
    permissionService
            .checkPermissionAll("Access token not allowed", readScopes);

    Project project = service.byOwnerAndName(username, name, fetches);

    return Optional.ofNullable(project)
            .map(peek(validator))
            .map(dtoMapper)
            .orElseThrow(() -> new NotFoundException(dto.Project.class.getSimpleName(), username + "/" + name));
  }

  @Override
  public PagedList<dto.Aggregate> activity(UUID id) {
    permissionService
            .checkPermissionAll("Access token not allowed", readScopes);

    return new DtoPagedList<>(
            logEntryService.getAggregates(new LogEntryCriteria().withProjectId(id)),
            AggregateMapper::toDto);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SearchResponse search(UUID projectId, SearchForm search) {
    permissionService.checkPermissionAll("Access token not allowed", readScopes);

    Messages messages = Context.current().messages();

    dto.Project project = get(projectId);

    search.setLimit(configuration.getInt("translatr.search.autocomplete.limit"));

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
                Data.from(Key.class, null, "???", null)));
      }

      if (permissionService.hasPermissionAny(project.id, ProjectRole.Owner, ProjectRole.Manager,
              ProjectRole.Developer) && permissionService.hasPermissionAll(Scope.KeyWrite)) {
        suggestions.add(Suggestable.DefaultSuggestable.from(
                messages.at("key.create", search.search),
                Data.from(Key.class, null, "+++", null)));
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
                Data.from(Locale.class, null, "???", null)));
      }

      if (permissionService.hasPermissionAny(project.id, ProjectRole.Owner, ProjectRole.Translator)
              && permissionService.hasPermissionAll(Scope.LocaleWrite)) {
        suggestions.add(Suggestable.DefaultSuggestable.from(
                messages.at("locale.create", search.search),
                Data.from(Locale.class, null, "+++", null)));
      }
    }

    return SearchResponse.from(SuggestionMapper.toDto(suggestions));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Project toModel(dto.Project in) {
    return ProjectMapper.toModel(in);
  }
}
