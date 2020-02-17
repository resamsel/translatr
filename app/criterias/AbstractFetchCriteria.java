package criterias;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractFetchCriteria<T extends AbstractFetchCriteria<T>> implements FetchCriteria {
  protected final T self;

  private final List<String> fetches = new ArrayList<>();

  public AbstractFetchCriteria() {
    this.self = (T) this;
  }

  /**
   * @return the fetches
   */
  @Override
  public List<String> getFetches() {
    return fetches;
  }

  private T withFetches(List<String> fetches) {
    this.fetches.addAll(fetches);
    return self;
  }

  public T withFetches(String... fetches) {
    if (fetches != null) {
      return withFetches(Arrays.asList(fetches));
    }
    return self;
  }

  public boolean hasFetch(String fetch) {
    return fetches.contains(fetch);
  }
}
