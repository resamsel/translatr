package services;

import com.google.inject.ImplementedBy;

import services.impl.PermissionServiceImpl;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 3 Oct 2016
 */
@ImplementedBy(PermissionServiceImpl.class)
public interface PermissionService
{

}
