package forms;

import models.Locale;
import play.data.Form;
import play.data.validation.Constraints;

/**
 *
 * @author resamsel
 * @version 2 Sep 2016
 */
public class LocaleForm extends SearchForm {
  @Constraints.MaxLength(Locale.NAME_LENGTH)
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
  public Locale into(Locale in) {
    in.name = name;

    return in;
  }

  /**
   * @param key
   * @return
   */
  public static Form<LocaleForm> with(Locale in, Form<LocaleForm> out) {
    out.get().name = in.name;

    return out;
  }

  /**
   * @param key
   * @return
   */
  public static Form<LocaleForm> with(String localeName, Form<LocaleForm> out) {
    out.get().name = localeName;

    return out;
  }
}
