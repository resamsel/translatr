package dto;

import org.joda.time.DateTime;

public class Aggregate extends Dto {
  public DateTime date;

  public long millis;

  public String key;

  public int value;

  public Aggregate(models.Aggregate in) {
    this.date = in.date;
    this.millis = in.millis;
    this.key = in.key;
    this.value = in.value;

    if (this.millis == 0 && in.date != null) {
      this.millis = in.date.getMillis();
    }
  }


  public static Aggregate from(models.Aggregate aggregate) {
    return new Aggregate(aggregate);
  }
}
