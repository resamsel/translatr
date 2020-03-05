package services.api.impl;

import com.avaje.ebean.PagedList;
import com.fasterxml.jackson.databind.JsonNode;
import criterias.AbstractSearchCriteria;
import dto.Dto;
import dto.DtoPagedList;
import dto.NotFoundException;
import models.Model;
import models.Scope;
import play.libs.Json;
import services.ModelService;
import services.PermissionService;
import services.api.ApiService;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author resamsel
 * @version 29 Jan 2017
 */
public abstract class AbstractApiService
    <MODEL extends Model<MODEL, ID>, ID, CRITERIA extends AbstractSearchCriteria<CRITERIA>,
        SERVICE extends ModelService<MODEL, ID, CRITERIA>, DTO extends Dto>
    implements ApiService<DTO, ID, CRITERIA> {

  protected SERVICE service;
  private Class<DTO> dtoClass;
  Function<MODEL, DTO> dtoMapper;
  Scope[] readScopes;
  private Scope[] writeScopes;
  protected final PermissionService permissionService;
  private final Validator validator;

  protected AbstractApiService(
      SERVICE service,
      Class<DTO> dtoClass,
      Function<MODEL, DTO> dtoMapper,
      Scope[] readScopes,
      Scope[] writeScopes,
      PermissionService permissionService,
      Validator validator
  ) {
    this.service = service;
    this.dtoClass = dtoClass;
    this.dtoMapper = dtoMapper;
    this.readScopes = readScopes;
    this.writeScopes = writeScopes;
    this.permissionService = permissionService;
    this.validator = validator;
  }

  /**
   * May be overridden to prepare the wrapper for dealing with the current request.
   *
   * @return the DTO mapper
   */
  protected Function<MODEL, DTO> getDtoMapper() {
    return dtoMapper;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PagedList<DTO> find(CRITERIA criteria) {
    return find(criteria, null);
  }

  @Override
  public PagedList<DTO> find(CRITERIA criteria, Consumer<CRITERIA> validator) {
    permissionService.checkPermissionAll("Access token not allowed", readScopes);

    if (validator != null) {
      validator.accept(criteria);
    }

    return new DtoPagedList<>(service.findBy(criteria), getDtoMapper());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DTO get(ID id, String... propertiesToFetch) {
    permissionService.checkPermissionAll("Access token not allowed", readScopes);

    MODEL obj = service.byId(id, propertiesToFetch);

    if (obj == null) {
      throw new NotFoundException(dtoClass.getSimpleName(), id);
    }

    return getDtoMapper().apply(obj);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DTO create(JsonNode in) {
    permissionService.checkPermissionAll("Access token not allowed", writeScopes);

    return getDtoMapper().apply(service.create(toModel(toDto(in))));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DTO update(JsonNode in) {
    permissionService.checkPermissionAll("Access token not allowed", writeScopes);

    return getDtoMapper().apply(service.update(toModel(toDto(in))));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DTO delete(ID id) {
    permissionService.checkPermissionAll("Access token not allowed", writeScopes);

    MODEL m = service.byId(id);

    if (m == null) {
      throw new NotFoundException(dtoClass.getSimpleName(), id);
    }

    DTO out = getDtoMapper().apply(m);

    service.delete(m);

    return out;
  }

  private DTO toDto(JsonNode json) {
    DTO dto = Json.fromJson(json, dtoClass);

    Set<ConstraintViolation<DTO>> violations = validator.validate(dto);

    if (!violations.isEmpty()) {
      throw new ConstraintViolationException(
          "Constraint violations detected: " + violations.stream().map(Object::toString).collect(
              Collectors.joining(",")),
          violations
      );
    }

    return dto;
  }

  protected abstract MODEL toModel(DTO dto);
}
