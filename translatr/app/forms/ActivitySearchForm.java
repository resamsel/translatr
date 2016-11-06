package forms;

import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;

/**
 * @author resamsel
 * @version 3 Nov 2016
 */
public class ActivitySearchForm extends SearchForm {
  public DateTime whenCreatedMin;

  public DateTime whenCreatedMax;

  public DateTime getWhenCreatedMin() {
    return whenCreatedMin;
  }

  public void setWhenCreatedMin(DateTime whenCreatedMin) {
    this.whenCreatedMin = whenCreatedMin;
  }

  public ActivitySearchForm withWhenCreatedMin(DateTime whenCreatedMin) {
    setWhenCreatedMin(whenCreatedMin);
    return this;
  }

  public DateTime getWhenCreatedMax() {
    return whenCreatedMax;
  }

  public void setWhenCreatedMax(DateTime whenCreatedMax) {
    this.whenCreatedMax = whenCreatedMax;
  }

  public ActivitySearchForm withWhenCreatedMax(DateTime whenCreatedMax) {
    setWhenCreatedMax(whenCreatedMax);
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected List<NameValuePair> parameters(int limit, int offset, String order, String search) {
    List<NameValuePair> parameters = super.parameters(limit, offset, order, search);

    if (whenCreatedMin != null)
      parameters.add(new BasicNameValuePair("whenCreatedMin", whenCreatedMin.toString()));
    if (whenCreatedMax != null)
      parameters.add(new BasicNameValuePair("whenCreatedMax", whenCreatedMax.toString()));

    return parameters;
  }
}
