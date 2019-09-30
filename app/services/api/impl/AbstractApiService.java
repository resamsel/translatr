package services.api.impl;

import com.avaje.ebean.PagedList;
import com.fasterxml.jackson.databind.JsonNode;
import criterias.AbstractSearchCriteria;
import dto.Dto;
import dto.DtoPagedList;
import dto.NotFoundException;
import models.Model;
import models.Scope;
import services.ModelService;
import services.PermissionService;
import services.api.ApiService;

import java.util.function.Consumer;
import java.util.function.Function;

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

  protected AbstractApiService(SERVICE service, Class<DTO> dtoClass,
      Function<MODEL, DTO> dtoMapper, Scope[] readScopes, Scope[] writeScopes,
      PermissionService permissionService) {

    this.service = service;
    this.dtoClass = dtoClass;
    this.dtoMapper = dtoMapper;
    this.readScopes = readScopes;
    this.writeScopes = writeScopes;
    this.permissionService = permissionService;
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

    return new DtoPagedList<>(service.findBy(criteria), dtoMapper);
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

    return dtoMapper.apply(obj);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DTO create(JsonNode in) {
    permissionService.checkPermissionAll("Access token not allowed", writeScopes);

    return dtoMapper.apply(service.create(toModel(in)));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DTO update(JsonNode in) {
    permissionService.checkPermissionAll("Access token not allowed", writeScopes);

    return dtoMapper.apply(service.update(toModel(in)));
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

    DTO out = dtoMapper.apply(m);

    service.delete(m);

    return out;
  }

  protected abstract MODEL toModel(JsonNode json);
}
