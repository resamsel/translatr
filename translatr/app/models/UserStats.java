package models;

/**
 * 
 * <p>
 *
 * @author resamsel
 * @version 18 Oct 2016
 */
public class UserStats
{
	public int numberOfProjects;

	public int numberOfActivities;

	private UserStats()
	{
	}

	public static UserStats create(int numberOfProjects, int numberOfActivities)
	{
		UserStats out = new UserStats();

		out.numberOfProjects = numberOfProjects;
		out.numberOfActivities = numberOfActivities;

		return out;
	}
}
