package services.api.impl;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.fasterxml.jackson.databind.JsonNode;

import criterias.AbstractSearchCriteria;
import dto.Dto;
import dto.NotFoundException;
import dto.PermissionException;
import models.Model;
import models.Scope;
import services.ModelService;
import services.api.ApiService;
import utils.PermissionUtils;

/**
 * @author resamsel
 * @version 29 Jan 2017
 */
public abstract class AbstractApiService<MODEL extends Model<MODEL, ID>, ID, CRITERIA extends AbstractSearchCriteria<CRITERIA>, DTO extends Dto>
    implements ApiService<DTO, ID, CRITERIA> {
  protected ModelService<MODEL, ID, CRITERIA> service;
  protected Class<DTO> dtoClass;
  protected Function<MODEL, DTO> dtoMapper;
  protected Function<JsonNode, MODEL> modelMapper;
  protected Scope[] readScopes;
  protected Scope[] writeScopes;

  protected AbstractApiService(ModelService<MODEL, ID, CRITERIA> service, Class<DTO> dtoClass,
      Function<MODEL, DTO> dtoMapper, Function<JsonNode, MODEL> modelMapper, Scope[] readScopes,
      Scope[] writeScopes) {
    this.service = service;
    this.dtoClass = dtoClass;
    this.dtoMapper = dtoMapper;
    this.modelMapper = modelMapper;
    this.readScopes = readScopes;
    this.writeScopes = writeScopes;
  }

  @Override
  public List<DTO> find(CRITERIA criteria,
      @SuppressWarnings("unchecked") Consumer<CRITERIA>... validators) {
    for (Consumer<CRITERIA> validator : validators)
      validator.accept(criteria);

    checkPermissionAll("Access token not allowed", readScopes);

    return service.findBy(criteria).getList().stream().map(dtoMapper).collect(toList());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DTO get(ID id) {
    checkPermissionAll("Access token not allowed", readScopes);

    MODEL obj = service.byId(id);

    if (obj == null)
      throw new NotFoundException(dtoClass.getSimpleName(), id);

    return dtoMapper.apply(obj);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DTO create(JsonNode in) {
    checkPermissionAll("Access token not allowed", writeScopes);

    return dtoMapper.apply(service.create(modelMapper.apply(in)));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DTO update(JsonNode in) {
    checkPermissionAll("Access token not allowed", writeScopes);

    return dtoMapper.apply(service.update(modelMapper.apply(in)));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DTO delete(ID id) {
    checkPermissionAll("Access token not allowed", writeScopes);

    MODEL m = service.byId(id);

    if (m == null)
      throw new NotFoundException(dtoClass.getSimpleName(), id);

    DTO out = dtoMapper.apply(m);

    service.delete(m);

    return out;
  }

  /**
   * @param errorMessage
   * @param scopes
   */
  protected void checkPermissionAll(String errorMessage, Scope... scopes) {
    if (!PermissionUtils.hasPermissionAll(scopes))
      throw new PermissionException(errorMessage, scopes);
  }
}
