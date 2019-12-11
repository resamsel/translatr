package repositories.impl;

import criterias.PagedListFactory;
import repositories.PagedListFactoryProvider;

public class PagedListFactoryProviderImpl implements PagedListFactoryProvider {
  @Override
  public PagedListFactory get() {
    return new PagedListFactory();
  }
}
