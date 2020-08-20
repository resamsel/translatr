package filters;

import com.google.common.collect.ImmutableList;
import play.http.HttpFilters;
import play.mvc.EssentialFilter;

import javax.inject.Inject;
import java.util.List;

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

  @Override
  public List<EssentialFilter> getFilters() {
    return ImmutableList.of(timingFilter, tlsFilter);
  }
}
