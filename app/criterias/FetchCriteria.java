package criterias;

import java.util.List;

public interface FetchCriteria {
  List<String> getFetches();

  boolean hasFetch(String fetch);
}
