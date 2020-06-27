package commands;

import play.inject.Injector;
import play.mvc.Call;

import java.io.Serializable;

public interface Command<T> extends Serializable {
  Command<T> with(T t);

  void execute(Injector injector);

  String getMessage();

  /**
   * @return
   */
  Call redirect();
}
