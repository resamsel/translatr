package forms;

import play.data.format.Formats;
import play.data.validation.Constraints;

/**
 * 
 * <p>
 *
 * @author resamsel
 * @version 6 Oct 2016
 */
public class Accept
{
	@Constraints.Required
	@Formats.NonEmpty
	public Boolean accept;

	public Boolean getAccept()
	{
		return accept;
	}

	public void setAccept(Boolean accept)
	{
		this.accept = accept;
	}
}
