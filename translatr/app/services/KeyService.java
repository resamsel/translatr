package services;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.inject.ImplementedBy;

import models.Key;
import services.impl.KeyServiceImpl;

/**
 *
 * @author resamsel
 * @version 29 Aug 2016
 */
@ImplementedBy(KeyServiceImpl.class)
public interface KeyService extends ModelService<Key>
{
	/**
	 * @param keyIds
	 * @param localesSize
	 * @return
	 */
	Map<UUID, Double> progress(List<UUID> keyIds, long localesSize);
}
