package assertions;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.Collection;
import org.apache.commons.lang3.StringUtils;
import org.fest.assertions.api.AbstractAssert;

/**
 * Created by resamsel on 24/07/2017.
 */
public abstract class AbstractGenericAssert<S extends AbstractGenericAssert<S, A>, A> extends
    AbstractAssert<S, A> {

  private final String name;

  protected AbstractGenericAssert(String name, A actual, Class<S> selfType) {
    super(actual, selfType);
    this.name = name;
  }

  protected S isNull(String field, Object actual) {
    assertThat(actual)
        .overridingErrorMessage("Expected %s's %s to be null, but was <%s> (%s)", name, field,
            actual, descriptionText())
        .isNull();
    return myself;
  }

  protected S isTrue(String field, Boolean actual) {
    assertThat(actual)
        .overridingErrorMessage("Expected %s's %s to be true, but was <%s> (%s)", name, field,
            actual, descriptionText())
        .isTrue();
    return myself;
  }

  protected S isFalse(String field, Boolean actual) {
    assertThat(actual)
        .overridingErrorMessage("Expected %s's %s to be false, but was <%s> (%s)", name, field,
            actual, descriptionText())
        .isFalse();
    return myself;
  }

  protected S isEqualTo(String field, int expected, int actual) {
    assertThat(actual)
        .overridingErrorMessage("Expected %s's %s to be <%s> but was <%s> (%s)", name, field,
            expected, actual, descriptionText())
        .isEqualTo(expected);
    return myself;
  }

  protected <T> S isEqualTo(String field, T expected, T actual) {
    assertThat(actual)
        .overridingErrorMessage("Expected %s's %s to be <%s> but was <%s> (%s)", name, field,
            expected, actual, descriptionText())
        .isEqualTo(expected);
    return myself;
  }

  protected S isEqualTo(String field, String expected, String actual) {
    assertThat(actual)
        .overridingErrorMessage("Expected %s's %s to be <%s> but was <%s> (%s)", name, field,
            expected, StringUtils.abbreviate(actual.trim(), 50), descriptionText())
        .isEqualTo(expected);
    return myself;
  }

  protected S contains(String field, String s, String actual) {
    assertThat(actual)
        .overridingErrorMessage("Expected %s's %s to contain <%s> but was <%s> (%s)", name, field,
            s, StringUtils.abbreviate(actual.trim(), 50), descriptionText()).contains(s);
    return myself;
  }

  protected S hasSize(String field, int expected, Collection<?> actual) {
    assertThat(actual)
        .overridingErrorMessage("Expected %s's %s to have a size of <%d> but was <%d> (%s)", name,
            field, expected, actual.size(), descriptionText()).hasSize(expected);
    return myself;
  }
}
