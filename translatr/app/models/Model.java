package models;

/**
 * @author resamsel
 * @version 10 Jan 2017
 */
public interface Model<T extends Model<T>> {
  T updateFrom(T in);
}
