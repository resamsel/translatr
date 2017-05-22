package dto;

import java.util.function.Function;

import com.avaje.ebean.PagedList;

/**
 * @author resamsel
 * @version 10 Feb 2017
 */
public class LocalesPaged extends DtoPagedList<models.Locale, Locale> {
  private static final long serialVersionUID = -1561133632023006929L;

  /**
   * @param delegate
   * @param mapper
   * 
   */
  public LocalesPaged(PagedList<models.Locale> delegate, Function<models.Locale, Locale> mapper) {
    super(delegate, mapper);
  }
}
