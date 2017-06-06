package modules;

import com.google.inject.AbstractModule;

import play.data.format.Formatters;
import utils.FormattersProvider;

/**
 * @author resamsel
 * @version 3 Nov 2016
 */
public class FormattersModule extends AbstractModule {
  /**
   * {@inheritDoc}
   */
  @Override
  protected void configure() {
    bind(Formatters.class).toProvider(FormattersProvider.class);
  }
}
