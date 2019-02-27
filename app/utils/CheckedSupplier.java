package utils;

import java.util.concurrent.CompletionException;
import java.util.function.Supplier;

public interface CheckedSupplier<T> {

  T get() throws Exception;

  default T wrap() {
    try {
      return get();
    } catch (Throwable t) {
      throw new CompletionException(t);
    }
  }
}
