package services;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.inject.ImplementedBy;

import models.Locale;
import services.impl.LocaleServiceImpl;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 29 Aug 2016
 */
@ImplementedBy(LocaleServiceImpl.class)
public interface LocaleService extends ModelService<Locale>
{

	/**
	 * @param localeIds
	 * @param keysSize
	 * @return
	 */
	Map<UUID, Double> progress(List<UUID> localeIds, long keysSize);
}
