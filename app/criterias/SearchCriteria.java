package criterias;

/**
 * @author resamsel
 * @version 19 Aug 2016
 */
public interface SearchCriteria extends ContextCriteria {
  String getSearch();

  String getOrder();

  Integer getLimit();

  Integer getOffset();

}
