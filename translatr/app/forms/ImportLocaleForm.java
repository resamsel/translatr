package forms;

import play.data.validation.Constraints.Required;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 30 Aug 2016
 */
public class ImportLocaleForm
{
	@Required
	public String type;
}
