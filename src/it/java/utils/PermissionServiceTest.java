package utils;

import org.mockito.Mockito;
import play.mvc.Http;
import services.PermissionService;
import tests.AbstractTest;

/**
 * @author resamsel
 * @version 27 Oct 2016
 */
public class PermissionServiceTest extends AbstractTest {

  private PermissionService permissionService;
  private Http.Request request;

  @Override
  protected void injectMembers() {
    permissionService = app.injector().instanceOf(PermissionService.class);
    request = Mockito.mock(Http.Request.class);
  }
}
