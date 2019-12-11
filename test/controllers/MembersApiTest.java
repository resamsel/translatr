package controllers;

import assertions.CustomAssertions;
import com.feth.play.module.pa.PlayAuthenticate;
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
import services.CacheService;
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
  private CacheService cache;
  @Mock
  private PlayAuthenticate auth;
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

  @Before
  public void setUp() {
    when(injector.instanceOf(HttpExecutionContext.class)).thenReturn(executionContext);
    when(executionContext.current()).thenReturn(new ForkJoinPool());
    when(contextProvider.get()).thenReturn(context);
    when(context.request()).thenReturn(request);

    target = new MembersApi(injector, cache, auth, projectUserApiService, contextProvider);
  }

  @Test
  public void find() throws ExecutionException, InterruptedException {
    // given
    UUID projectId = UUID.randomUUID();

    when(projectUserApiService.find(any())).thenReturn(PagedListFactory.create(Collections.emptyList()));

    // when
    Result actual = target.find(projectId).toCompletableFuture().get();

    // then
    CustomAssertions.assertThat(actual)
        .statusIsEqualTo(200)
        .isNotNull();
  }
}
