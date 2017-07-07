package forms;

import models.Key;
import play.data.Form;
import play.data.validation.Constraints;

/**
 *
 * @author resamsel
 * @version 2 Sep 2016
 */
public class KeyForm extends SearchForm {
  @Constraints.MinLength(0)
  @Constraints.MaxLength(Key.NAME_LENGTH)
  private String name;

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @param in
   * @return
   */
  public Key into(Key in) {
    in.name = name;

    return in;
  }

  /**
   * @param in
   * @param out
   * @return
   */
  public static Form<KeyForm> with(Key in, Form<KeyForm> out) {
    out.get().name = in.name;

    return out;
  }

  /**
   * @param key
   * @return
   */
  public static Form<KeyForm> with(String keyName, Form<KeyForm> out) {
    out.get().name = keyName;

    return out;
  }
}
