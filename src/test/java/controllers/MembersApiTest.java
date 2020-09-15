package controllers;

import assertions.CustomAssertions;
import criterias.PagedListFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import play.inject.Injector;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;
import play.mvc.Result;
import services.AuthProvider;
import services.ContextProvider;
import services.api.ProjectUserApiService;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MembersApiTest {
  private MembersApi target;

  @Mock
  private Injector injector;
  @Mock
  private ProjectUserApiService projectUserApiService;
  @Mock
  private HttpExecutionContext executionContext;
  @Mock
  private Http.Context context;
  @Mock
  private ContextProvider contextProvider;
  @Mock
  private Http.Request request;
  @Mock
  private AuthProvider authProvider;

  @Before
  public void setUp() {
    when(injector.instanceOf(HttpExecutionContext.class)).thenReturn(executionContext);
    when(executionContext.current()).thenReturn(new ForkJoinPool());
    when(contextProvider.get()).thenReturn(context);
    when(context.request()).thenReturn(request);

    target = new MembersApi(injector, authProvider, projectUserApiService, contextProvider);
  }

  @Test
  public void find() throws ExecutionException, InterruptedException {
    // given
    UUID projectId = UUID.randomUUID();

    when(projectUserApiService.find(any(), any())).thenReturn(PagedListFactory.create(Collections.emptyList()));

    // when
    Result actual = target.find(projectId).toCompletableFuture().get();

    // then
    CustomAssertions.assertThat(actual)
            .statusIsEqualTo(200)
            .isNotNull();
  }
}
