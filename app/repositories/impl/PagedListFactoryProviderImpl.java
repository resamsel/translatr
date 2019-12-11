package repositories.impl;

import criterias.PagedListFactory;
import repositories.PagedListFactoryProvider;

import javax.inject.Singleton;

@Singleton
public class PagedListFactoryProviderImpl implements PagedListFactoryProvider {
  @Override
  public PagedListFactory get() {
    return new PagedListFactory();
  }
}
