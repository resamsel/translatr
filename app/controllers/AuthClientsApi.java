package controllers;

import dto.AuthClient;
import dto.errors.GenericError;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import mappers.AuthClientMapper;
import play.inject.Injector;
import play.mvc.Result;
import services.api.AuthClientApiService;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class AuthClientsApi extends AbstractBaseApi {
  private static final String FIND = "Find auth clients";
  private static final String FIND_RESPONSE = "Found auth clients";
  private final AuthClientApiService authClientApiService;

  @Inject
  public AuthClientsApi(Injector injector, AuthClientApiService authClientApiService) {
    super(injector);

    this.authClientApiService = authClientApiService;
  }

  @ApiResponses({
      @ApiResponse(code = 200, message = FIND_RESPONSE, response = AuthClient[].class),
      @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
  public CompletionStage<Result> find() {
    return toJsonList(() -> authClientApiService
        .getClients()
        .stream()
        .map(AuthClientMapper::toDto)
        .collect(Collectors.toList())
    );
  }
}
