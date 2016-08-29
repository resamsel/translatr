package commands;

import play.mvc.Call;

public interface Command
{
	void execute();

	String getMessage();

	/**
	 * @return
	 */
	Call redirect();
}
