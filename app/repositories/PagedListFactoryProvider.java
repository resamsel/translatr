package repositories;

import com.google.inject.ImplementedBy;
import criterias.PagedListFactory;
import repositories.impl.PagedListFactoryProviderImpl;

@ImplementedBy(PagedListFactoryProviderImpl.class)
public interface PagedListFactoryProvider {
  PagedListFactory get();
}
