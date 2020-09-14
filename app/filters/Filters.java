package filters;

import com.google.common.collect.ImmutableList;
import org.pac4j.play.filters.SecurityFilter;
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
  private final SecurityFilter securityFilter;

  @Inject
  public Filters(TimingFilter timingFilter, ForceSSLFilter tlsFilter, SecurityFilter securityFilter) {
    this.timingFilter = timingFilter;
    this.tlsFilter = tlsFilter;
    this.securityFilter = securityFilter;
  }

  @Override
  public List<EssentialFilter> getFilters() {
    return ImmutableList.of(timingFilter, tlsFilter, securityFilter.asJava());
  }
}
