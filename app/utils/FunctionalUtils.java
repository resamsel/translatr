package utils;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public final class FunctionalUtils {
  private FunctionalUtils() {
  }

  /**
   * Peeks at the current element and consumes it with the given consumer.
   *
   * @param consumer the consuming operation
   * @param <T>      the type of the element to peek at
   * @return the element that was peeked at
   */
  public static <T> UnaryOperator<T> peek(Consumer<T> consumer) {
    return t -> {
      consumer.accept(t);
      return t;
    };
  }
}
