package controllers;

import com.avaje.ebean.PagedList;
import com.fasterxml.jackson.databind.node.NullNode;
import com.feth.play.module.pa.PlayAuthenticate;
import dto.NotFoundException;
import dto.PermissionException;
import dto.SearchResponse;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.inject.Injector;
import play.libs.Json;
import play.mvc.Result;
import services.CacheService;
import utils.ErrorUtils;

/**
 * @author resamsel
 * @version 23 May 2017
 */
public class AbstractBaseApi extends AbstractController {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractBaseApi.class);

  static final String PERMISSION_ERROR = "Invalid access token";
  static final String INTERNAL_SERVER_ERROR = "Internal server error";
  static final String INPUT_ERROR = "Bad request";

  protected static final String ACCESS_TOKEN = "The access token";
  static final String PARAM_ACCESS_TOKEN = "access_token";
  static final String PARAM_SEARCH = "search";
  static final String OFFSET = "The first row of the paged result list";
  static final String PARAM_OFFSET = "offset";
  static final String LIMIT = "The page size of the paged result list";
  static final String PARAM_LIMIT = "limit";
  static final String FETCH = "The fields to fetch additionally, separated by commas";
  static final String PARAM_FETCH = "fetch";
  static final String PROJECT_ID = "The project ID";
  static final String PROJECT_NAME = "The project name";
  static final String LOCALE_ID = "The locale ID";
  static final String PARAM_LOCALE_ID = "localeId";
  static final String KEY_ID = "The key ID";
  static final String MESSAGE_ID = "The message ID";
  static final String USER_ID = "The user ID";
  static final String USER_USERNAME = "The user username";

  static final String AUTHORIZATION = "scopes";

  static final String PROJECT_READ = "project:read";
  static final String PROJECT_READ_DESCRIPTION = "Read project";
  static final String PROJECT_WRITE = "project:write";
  static final String PROJECT_WRITE_DESCRIPTION = "Write project";
  static final String LOCALE_READ = "locale:read";
  static final String LOCALE_READ_DESCRIPTION = "Read locale";
  static final String LOCALE_WRITE = "locale:write";
  static final String LOCALE_WRITE_DESCRIPTION = "Write locale";
  static final String KEY_READ = "key:read";
  static final String KEY_READ_DESCRIPTION = "Read key";
  static final String KEY_WRITE = "key:write";
  static final String KEY_WRITE_DESCRIPTION = "Write key";
  static final String MESSAGE_READ = "message:read";
  static final String MESSAGE_READ_DESCRIPTION = "Read message";
  static final String MESSAGE_WRITE = "message:write";
  static final String MESSAGE_WRITE_DESCRIPTION = "Write message";
  static final String USER_READ = "user:read";
  static final String USER_READ_DESCRIPTION = "Read user";
  static final String USER_WRITE = "user:write";
  static final String USER_WRITE_DESCRIPTION = "Write user";

  protected AbstractBaseApi(Injector injector, CacheService cache, PlayAuthenticate auth) {
    super(injector, cache, auth);
  }

  protected <IN> CompletionStage<Result> toJson(Supplier<IN> supplier) {
    return CompletableFuture.supplyAsync(supplier, executionContext.current())
        .thenApply(out -> ok(Optional.ofNullable(out).map(Json::toJson).orElse(NullNode.getInstance())))
        .exceptionally(this::handleException);
  }

  <T> CompletionStage<Result> toJsons(Supplier<PagedList<T>> supplier) {
    return CompletableFuture.supplyAsync(supplier, executionContext.current())
        .thenApply(out -> ok(Json.toJson(out))).exceptionally(this::handleException);
  }

  CompletionStage<Result> toJsonSearch(Supplier<SearchResponse> supplier) {
    return CompletableFuture.supplyAsync(supplier, executionContext.current())
        .thenApply(out -> ok(Json.toJson(out))).exceptionally(this::handleException);
  }

  @Override
  protected Result handleException(Throwable t) {
    try {
      throw ExceptionUtils.getRootCause(t);
    } catch (PermissionException e) {
      return forbidden(ErrorUtils.toJson(e));
    } catch (NotFoundException e) {
      return notFound(ErrorUtils.toJson(e));
    } catch (ConstraintViolationException e) {
      LOGGER.debug("Handling constraint violation", t);
      return badRequest(ErrorUtils.toJson(e));
    } catch (ValidationException e) {
      LOGGER.debug("Handling validation", t);
      return badRequest(ErrorUtils.toJson(e));
    } catch (Throwable e) {
      LOGGER.error("Error while processing API request", e);
      return internalServerError(ErrorUtils.toJson(e));
    }
  }
}
