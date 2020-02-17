package criterias;

public class DefaultFetchCriteria extends AbstractFetchCriteria<DefaultFetchCriteria> {
  public DefaultFetchCriteria(String... fetches) {
    withFetches(fetches);
  }
}
