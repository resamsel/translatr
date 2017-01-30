package services.api;

import java.util.UUID;

import com.google.inject.ImplementedBy;

import criterias.LocaleCriteria;
import dto.Locale;
import play.mvc.Http.Request;
import services.api.impl.LocaleApiServiceImpl;

/**
 * @author resamsel
 * @version 29 Jan 2017
 */
@ImplementedBy(LocaleApiServiceImpl.class)
public interface LocaleApiService extends ApiService<Locale, UUID, LocaleCriteria> {
  Locale upload(UUID localeId, String fileType, Request request);

  byte[] download(UUID localeId, String fileType);
}
