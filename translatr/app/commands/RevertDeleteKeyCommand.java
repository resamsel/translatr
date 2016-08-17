package commands;

import java.util.stream.Collectors;

import com.avaje.ebean.Ebean;

import dto.Key;

public class RevertDeleteKeyCommand implements Command {
	private Key key;

	public RevertDeleteKeyCommand(models.Key key) {
		this.key = new Key(key);
	}

	@Override
	public void execute() {
		Ebean.save(key.toModel());
		Ebean.save(key.messages.stream().map(m -> m.toModel()).collect(Collectors.toList()));
	}
}
