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
import play.i18n.MessagesApi;
import play.mvc.Http;
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
import java.util.stream.Collectors;

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
  private final MessagesApi messagesApi;

  @Inject
  protected ProjectApiServiceImpl(
          Config configuration, ProjectService projectService,
          LocaleService localeService, KeyService keyService, LogEntryService logEntryService,
          PermissionService permissionService, AuthProvider authProvider, Validator validator,
          MessagesApi messagesApi, ProjectMapper projectMapper) {
    super(projectService, dto.Project.class, projectMapper::toDto,
            new Scope[]{Scope.ProjectRead},
            new Scope[]{Scope.ProjectWrite},
            permissionService,
            validator);

    this.configuration = configuration;
    this.localeService = localeService;
    this.keyService = keyService;
    this.logEntryService = logEntryService;
    this.authProvider = authProvider;
    this.messagesApi = messagesApi;
  }

  @Override
  public PagedList<dto.Project> find(ProjectCriteria criteria) {
    User loggedInUser = authProvider.loggedInUser(criteria.getRequest());
    if (loggedInUser != null && loggedInUser.role != UserRole.Admin) {
      criteria.withMemberId(loggedInUser.id);
    }

    return super.find(criteria);
  }

  @Override
  public dto.Project byOwnerAndName(Http.Request request, String username, String name, @Nonnull Consumer<Project> validator, String... fetches) {
    permissionService
            .checkPermissionAll(request, "Access token not allowed", readScopes);

    Project project = service.byOwnerAndName(username, name, request, fetches);

    return Optional.ofNullable(project)
            .map(peek(validator))
            .map(getDtoMapper(request))
            .orElseThrow(() -> new NotFoundException(dto.Project.class.getSimpleName(), username + "/" + name));
  }

  @Override
  public PagedList<dto.Aggregate> activity(Http.Request request, UUID id) {
    permissionService
            .checkPermissionAll(request, "Access token not allowed", readScopes);

    return new DtoPagedList<>(
            logEntryService.getAggregates(new LogEntryCriteria().withProjectId(id)),
            AggregateMapper::toDto);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SearchResponse search(Http.Request request, UUID projectId, SearchForm search) {
    permissionService.checkPermissionAll(request, "Access token not allowed", readScopes);

    Messages messages = messagesApi.preferred(request);

    dto.Project project = get(request, projectId);

    search.setLimit(configuration.getInt("translatr.search.autocomplete.limit"));

    List<Suggestable> suggestions = new ArrayList<>();

    if (permissionService.hasPermissionAll(request, Scope.KeyRead)) {
      PagedList<Key> keys = keyService
              .findBy(KeyCriteria.from(search).withProjectId(project.id).withOrder("whenUpdated desc"));

      search.pager(keys);

      if (!keys.getList().isEmpty()) {
        suggestions.addAll(keys
                .getList()
                .stream()
                .map(key -> Suggestable.DefaultSuggestable.from(
                        messages.at("key.autocomplete", key.name),
                        Data.from(Key.class, key.id, key.name, null)
                ))
                .collect(Collectors.toList()));
      }
      if (search.hasMore) {
        suggestions.add(Suggestable.DefaultSuggestable.from(
                messages.at("key.search", search.search),
                Data.from(Key.class, null, "???", null)));
      }

      if (permissionService.hasPermissionAny(request, project.id, ProjectRole.Owner, ProjectRole.Manager,
              ProjectRole.Developer) && permissionService.hasPermissionAll(request, Scope.KeyWrite)) {
        suggestions.add(Suggestable.DefaultSuggestable.from(
                messages.at("key.create", search.search),
                Data.from(Key.class, null, "+++", null)));
      }
    }

    if (permissionService.hasPermissionAll(request, Scope.LocaleRead)) {
      PagedList<Locale> locales = localeService.findBy(new LocaleCriteria()
              .withProjectId(project.id).withSearch(search.search).withOrder("whenUpdated desc"));

      search.pager(locales);
      if (!locales.getList().isEmpty()) {
        suggestions.addAll(locales.getList()
                .stream()
                .map(locale -> Suggestable.DefaultSuggestable.from(
                        messages.at("locale.autocomplete", locale.name),
                        Data.from(Locale.class, locale.id, locale.name, null)
                ))
                .collect(Collectors.toList()));
      }
      if (search.hasMore) {
        suggestions.add(Suggestable.DefaultSuggestable.from(
                messages.at("locale.search", search.search),
                Data.from(Locale.class, null, "???", null)));
      }

      if (permissionService.hasPermissionAny(request, project.id, ProjectRole.Owner, ProjectRole.Translator)
              && permissionService.hasPermissionAll(request, Scope.LocaleWrite)) {
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
