package services;

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
}
