package models;

/**
 * @author resamsel
 * @version 10 Jan 2017
 */
public interface Model<T extends Model<T, ID>, ID> {
  ID getId();

  T updateFrom(T in);
}
