package utils;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 7 Sep 2016
 */
public class TemplateInfo
{
	public String title;

	public String template;

	private TemplateInfo(String title, String template)
	{
		this.title = title;
		this.template = template;
	}

	public static TemplateInfo from(String title, String template)
	{
		return new TemplateInfo(title, template);
	}

	public static TemplateInfo from(String title)
	{
		return new TemplateInfo(title, null);
	}
}
