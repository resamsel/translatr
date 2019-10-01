package controllers;

import com.feth.play.module.pa.PlayAuthenticate;
import dto.errors.GenericError;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import mappers.AuthProviderMapper;
import play.inject.Injector;
import play.mvc.Result;
import services.CacheService;
import services.api.AuthProviderApiService;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class AuthProvidersApi extends AbstractBaseApi {
  private static final String FIND = "Find auth providers";
  private static final String FIND_RESPONSE = "Found auth providers";
  private final AuthProviderApiService authProviderApiService;

  @Inject
  public AuthProvidersApi(Injector injector, CacheService cache, PlayAuthenticate auth,
                          AuthProviderApiService authProviderApiService) {
    super(injector, cache, auth);

    this.authProviderApiService = authProviderApiService;
  }

  @ApiResponses({
      @ApiResponse(code = 200, message = FIND_RESPONSE, response = dto.AuthProvider[].class),
      @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
  public CompletionStage<Result> find() {
    return toJsonList(() -> authProviderApiService
        .getProviders()
        .stream()
        .map(AuthProviderMapper::toDto)
        .collect(Collectors.toList())
    );
  }
}
