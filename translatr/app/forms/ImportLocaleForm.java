package forms;

import play.data.validation.Constraints.Required;

/**
 *
 * @author resamsel
 * @version 30 Aug 2016
 */
public class ImportLocaleForm
{
	@Required
	private String fileType;

	/**
	 * @return the fileType
	 */
	public String getFileType()
	{
		return fileType;
	}

	/**
	 * @param fileType the fileType to set
	 */
	public void setFileType(String fileType)
	{
		this.fileType = fileType;
	}
}
