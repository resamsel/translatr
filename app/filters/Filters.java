package filters;

import javax.inject.Inject;

import play.http.HttpFilters;
import play.mvc.EssentialFilter;

/**
 *
 * @author resamsel
 * @version 13 Jul 2016
 */
public class Filters implements HttpFilters {
  private final TimingFilter timingFilter;
  private final ForceSSLFilter tlsFilter;

  /**
   * 
   */
  @Inject
  public Filters(TimingFilter timingFilter, ForceSSLFilter tlsFilter) {
    this.timingFilter = timingFilter;
    this.tlsFilter = tlsFilter;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public EssentialFilter[] filters() {
    return new EssentialFilter[] {timingFilter, tlsFilter};
  }
}
