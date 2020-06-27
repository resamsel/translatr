package forms;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.List;

/**
 * @author resamsel
 * @version 2 Nov 2016
 */
public class KeySearchForm extends SearchForm {
  public Boolean missing;

  /**
   * @return the missing
   */
  public boolean isMissing() {
    return missing;
  }

  /**
   * @param missing the missing to set
   */
  public void setMissing(boolean missing) {
    this.missing = missing;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected List<NameValuePair> parameters(int limit, int offset, String order, String search) {
    List<NameValuePair> parameters = super.parameters(limit, offset, order, search);

    parameters.add(new BasicNameValuePair("missing", String.valueOf(missing)));

    return parameters;
  }
}
