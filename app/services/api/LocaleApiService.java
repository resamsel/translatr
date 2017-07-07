package services.api;

import com.google.inject.ImplementedBy;
import criterias.LocaleCriteria;
import dto.Locale;
import java.util.UUID;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import services.api.impl.LocaleApiServiceImpl;

/**
 * @author resamsel
 * @version 29 Jan 2017
 */
@ImplementedBy(LocaleApiServiceImpl.class)
public interface LocaleApiService extends ApiService<Locale, UUID, LocaleCriteria> {
  Locale upload(UUID localeId, Request request);

  byte[] download(UUID localeId, String fileType, Response response);
}
