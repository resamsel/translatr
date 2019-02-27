package services.api;

import com.google.inject.ImplementedBy;
import criterias.KeyCriteria;
import dto.Key;
import java.util.UUID;
import services.api.impl.KeyApiServiceImpl;

/**
 * @author resamsel
 * @version 29 Jan 2017
 */
@ImplementedBy(KeyApiServiceImpl.class)
public interface KeyApiService extends ApiService<Key, UUID, KeyCriteria> {
}
