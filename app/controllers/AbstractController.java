package controllers;

import dto.PermissionException;
import exceptions.KeyNotFoundException;
import exceptions.LocaleNotFoundException;
import exceptions.ProjectNotFoundException;
import exceptions.UserNotFoundException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.LoggerFactory;
import play.inject.Injector;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import utils.CheckedSupplier;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author resamsel
 * @version 16 Sep 2016
 */
public abstract class AbstractController extends Controller {

  public static final String DEFAULT_SEARCH = null;
  public static final String DEFAULT_ORDER = "name";

  final HttpExecutionContext executionContext;

  protected AbstractController(Injector injector) {
    this.executionContext = injector.instanceOf(HttpExecutionContext.class);
  }

  <T> CompletionStage<T> async(CheckedSupplier<T> supplier) {
    return CompletableFuture.supplyAsync(supplier::wrap, executionContext.current());
  }

  protected Result handleException(Throwable t) {
    try {
      throw ExceptionUtils.getRootCause(t);
    } catch (UserNotFoundException e) {
      return notFound(e.getUsername());
    } catch (ProjectNotFoundException e) {
      return notFound(e.getUsername(), e.getProjectName());
    } catch (LocaleNotFoundException e) {
      return notFound(String.format("%s/%s/%s", e.getUsername(), e.getProjectName(), e.getLocaleName()));
    } catch (KeyNotFoundException e) {
      return notFound(String.format("%s/%s/%s", e.getUsername(), e.getProjectName(), e.getKeyName()));
    } catch (PermissionException e) {
      return forbidden(views.html.errors.restricted.render());
    } catch (Throwable e) {
      LoggerFactory.getLogger(AbstractApi.class).error("Error while processing request", e);
      return internalServerError(views.html.errors.restricted.render());
    }
  }
}
