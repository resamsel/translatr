package services.api;

import com.google.inject.ImplementedBy;
import criterias.LocaleCriteria;
import dto.Locale;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.mvc.Result;
import services.api.impl.LocaleApiServiceImpl;

import java.util.UUID;

/**
 * @author resamsel
 * @version 29 Jan 2017
 */
@ImplementedBy(LocaleApiServiceImpl.class)
public interface LocaleApiService extends ApiService<Locale, UUID, LocaleCriteria> {
  Locale upload(UUID localeId, Request request);

  Result download(Request request, UUID localeId, String fileType);

  Locale byOwnerAndProjectAndName(Http.Request request, String username, String projectName, String localeName,
                                  String... fetches);
}
