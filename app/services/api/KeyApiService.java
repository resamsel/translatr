package services.api;

import com.google.inject.ImplementedBy;
import criterias.KeyCriteria;
import dto.Key;
import services.api.impl.KeyApiServiceImpl;

import java.util.UUID;

/**
 * @author resamsel
 * @version 29 Jan 2017
 */
@ImplementedBy(KeyApiServiceImpl.class)
public interface KeyApiService extends ApiService<Key, UUID, KeyCriteria> {

  Key byOwnerAndProjectAndName(String username, String projectName, String keyName, String... fetches);
}
