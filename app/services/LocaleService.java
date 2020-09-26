package services;

import com.google.inject.ImplementedBy;
import criterias.LocaleCriteria;
import models.Locale;
import models.Project;
import play.mvc.Http;
import services.impl.LocaleServiceImpl;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author resamsel
 * @version 29 Aug 2016
 */
@ImplementedBy(LocaleServiceImpl.class)
public interface LocaleService extends ModelService<Locale, UUID, LocaleCriteria> {

  Locale byProjectAndName(Project project, String name, Http.Request request);

  void increaseWordCountBy(UUID localeId, int wordCountDiff, Http.Request request);

  void resetWordCount(UUID projectId);

  Locale byOwnerAndProjectAndName(String username, String projectName, String localeName, Http.Request request,
      String... fetches);
}
