package utils;

import com.typesafe.config.Config;
import forms.AccessTokenForm;
import forms.ActivitySearchForm;
import forms.KeyForm;
import forms.KeySearchForm;
import forms.LocaleForm;
import forms.LocaleSearchForm;
import forms.SearchForm;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.ValidationError;
import play.mvc.Http;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

/**
 * @author resamsel
 * @version 2 Nov 2016
 */
public class FormUtils {

  protected static <T> Form<T> bindFromRequest(FormFactory formFactory, Class<T> formClass, Http.Request request) {
    return formFactory.form(formClass).bindFromRequest(request);
  }

  public static <T> Form<T> include(Form<T> form, Throwable t) {
    if (t instanceof ConstraintViolationException) {
      return include(form, (ConstraintViolationException) t);
    }
    return form;
  }

  public static <T> Form<T> include(Form<T> form, ConstraintViolationException e) {
    e.getConstraintViolations().forEach(violation -> form
            .withError(new ValidationError(pathFrom(violation), violation.getMessage())));
    return form;
  }

  private static String pathFrom(ConstraintViolation<?> violation) {
    if (violation.getPropertyPath() != null) {
      return violation.getPropertyPath().toString();
    }
    return "name";
  }

  public static class Search {

    public static Form<SearchForm> bindFromRequest(FormFactory formFactory,
                                                   Config configuration,
                                                   Http.Request request) {
      return init(FormUtils.bindFromRequest(formFactory, SearchForm.class, request), configuration);
    }

    protected static <T extends SearchForm> Form<T> init(Form<T> form,
                                                         Config configuration) {
      T obj = form.get();

      if (obj.limit == null) {
        obj.limit = configuration.getInt("translatr.search.limit");
      }

      return form;
    }
  }

}
