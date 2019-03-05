package services.api;

import com.google.inject.ImplementedBy;
import criterias.LogEntryCriteria;
import dto.Activity;
import services.api.impl.ActivityApiServiceImpl;

import java.util.UUID;

/**
 * @author resamsel
 * @version 29 Jan 2017
 */
@ImplementedBy(ActivityApiServiceImpl.class)
public interface ActivityApiService extends ApiService<Activity, UUID, LogEntryCriteria> {
}
