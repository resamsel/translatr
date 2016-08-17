package commands;

import java.util.stream.Collectors;

import com.avaje.ebean.Ebean;

import dto.Locale;

public class RevertDeleteLocaleCommand implements Command {
	private Locale locale;

	public RevertDeleteLocaleCommand(models.Locale locale) {
		this.locale = new Locale(locale);
	}

	@Override
	public void execute() {
		Ebean.save(locale.toModel());
		Ebean.save(locale.messages.stream().map(m -> m.toModel()).collect(Collectors.toList()));
	}
}
