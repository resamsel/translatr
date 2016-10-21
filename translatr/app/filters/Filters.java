package filters;

import javax.inject.Inject;

import play.http.HttpFilters;
import play.mvc.EssentialFilter;

/**
 * 
 * <p>
 *
 * @author resamsel
 * @version 13 Jul 2016
 */
public class Filters implements HttpFilters
{
	private final TimingFilter timingFilter;

	/**
	 * 
	 */
	@Inject
	public Filters(TimingFilter timingFilter)
	{
		this.timingFilter = timingFilter;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EssentialFilter[] filters()
	{
		return new EssentialFilter[]{timingFilter};
	}
}
