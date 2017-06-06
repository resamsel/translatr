package services;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.inject.ImplementedBy;

import criterias.LocaleCriteria;
import models.Locale;
import services.impl.LocaleServiceImpl;

/**
 *
 * @author resamsel
 * @version 29 Aug 2016
 */
@ImplementedBy(LocaleServiceImpl.class)
public interface LocaleService extends ModelService<Locale, UUID, LocaleCriteria> {
  /**
   * @param localeIds
   * @param keysSize
   * @return
   */
  Map<UUID, Double> progress(List<UUID> localeIds, long keysSize);

  /**
   * @param localeId
   * @param wordCountDiff
   */
  void increaseWordCountBy(UUID localeId, int wordCountDiff);

  /**
   * @param projectId
   */
  void resetWordCount(UUID projectId);
}
