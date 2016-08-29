package services;

import com.google.inject.ImplementedBy;

import models.Key;
import services.impl.KeyServiceImpl;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 29 Aug 2016
 */
@ImplementedBy(KeyServiceImpl.class)
public interface KeyService extends ModelService<Key>
{
}
