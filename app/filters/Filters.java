package filters;

import com.google.common.collect.ImmutableList;
import org.pac4j.play.filters.SecurityFilter;
import play.Environment;
import play.http.HttpFilters;
import play.mvc.EssentialFilter;

import javax.inject.Inject;
import java.util.List;

/**
 * @author resamsel
 * @version 13 Jul 2016
 */
public class Filters implements HttpFilters {
  private final List<EssentialFilter> filters;

  @Inject
  public Filters(Environment environment, TimingFilter timingFilter, ForceSSLFilter tlsFilter, ForceDevServerFilter devServerFilter, SecurityFilter securityFilter) {
    if (environment.isDev()) {
      this.filters = ImmutableList.of(timingFilter, tlsFilter, devServerFilter, securityFilter.asJava());
    } else {
      this.filters = ImmutableList.of(timingFilter, tlsFilter, securityFilter.asJava());
    }
  }

  @Override
  public List<EssentialFilter> getFilters() {
    return filters;
  }
}
