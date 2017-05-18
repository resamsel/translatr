package services.api;

import java.util.UUID;

import com.google.inject.ImplementedBy;

import criterias.KeyCriteria;
import dto.Key;
import services.api.impl.KeyApiServiceImpl;

/**
 * @author resamsel
 * @version 29 Jan 2017
 */
@ImplementedBy(KeyApiServiceImpl.class)
public interface KeyApiService extends ApiService<Key, UUID, KeyCriteria> {
}
