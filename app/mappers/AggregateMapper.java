package mappers;

import dto.Aggregate;

public class AggregateMapper {
  public static Aggregate toDto(models.Aggregate in) {
    Aggregate out = new Aggregate();

    out.date = in.date;
    out.millis = in.millis;
    out.key = in.key;
    out.value = in.value;

    if (out.millis == 0 && in.date != null) {
      out.millis = in.date.getMillis();
    }

    return out;
  }
}
