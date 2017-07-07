package commands;

import java.io.Serializable;
import play.inject.Injector;
import play.mvc.Call;

public interface Command<T> extends Serializable {
  Command<T> with(T t);

  void execute(Injector injector);

  String getMessage();

  /**
   * @return
   */
  Call redirect();
}
