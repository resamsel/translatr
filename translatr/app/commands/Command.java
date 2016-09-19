package commands;

import play.mvc.Call;

public interface Command<T>
{
	Command<T> with(T t);

	void execute();

	String getMessage();

	/**
	 * @return
	 */
	Call redirect();
}
