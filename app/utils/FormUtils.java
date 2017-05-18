package utils;

import forms.ActivitySearchForm;
import forms.KeySearchForm;
import forms.LocaleSearchForm;
import forms.SearchForm;
import play.Configuration;
import play.data.Form;
import play.data.FormFactory;

/**
 * @author resamsel
 * @version 2 Nov 2016
 */
public class FormUtils {
  protected static <T> Form<T> bindFromRequest(FormFactory formFactory, Class<T> formClass) {
    return formFactory.form(formClass).bindFromRequest();
  }

  public static class Search {
    public static Form<SearchForm> bindFromRequest(FormFactory formFactory,
        Configuration configuration) {
      return init(FormUtils.bindFromRequest(formFactory, SearchForm.class), configuration);
    }

    /**
     * @param form
     * @param configuration
     * @return
     */
    protected static <T extends SearchForm> Form<T> init(Form<T> form,
        Configuration configuration) {
      T obj = form.get();

      if (obj.limit == null)
        obj.limit = configuration.getInt("translatr.search.limit", 20);

      return form;
    }
  }

  public static class LocaleSearch {
    public static Form<LocaleSearchForm> bindFromRequest(FormFactory formFactory,
        Configuration configuration) {
      return init(FormUtils.bindFromRequest(formFactory, LocaleSearchForm.class), configuration);
    }

    protected static Form<LocaleSearchForm> init(Form<LocaleSearchForm> form,
        Configuration configuration) {
      LocaleSearchForm obj = form.get();

      if (obj.missing == null)
        obj.missing = configuration.getBoolean("translatr.search.missing", false);

      return Search.init(form, configuration);
    }
  }

  public static class KeySearch {
    public static Form<KeySearchForm> bindFromRequest(FormFactory formFactory,
        Configuration configuration) {
      return init(FormUtils.bindFromRequest(formFactory, KeySearchForm.class), configuration);
    }

    protected static Form<KeySearchForm> init(Form<KeySearchForm> form,
        Configuration configuration) {
      KeySearchForm obj = form.get();

      if (obj.missing == null)
        obj.missing = configuration.getBoolean("translatr.search.missing", false);

      return Search.init(form, configuration);
    }
  }

  public static class ActivitySearch {
    public static Form<ActivitySearchForm> bindFromRequest(FormFactory formFactory,
        Configuration configuration) {
      return Search.init(FormUtils.bindFromRequest(formFactory, ActivitySearchForm.class),
          configuration);
    }
  }
}
