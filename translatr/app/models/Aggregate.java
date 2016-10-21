package models;

import javax.persistence.Entity;

import org.joda.time.DateTime;

import com.avaje.ebean.annotation.Sql;

/**
 * 
 * <p>
 *
 * @author resamsel
 * @version 28 Aug 2016
 */
@Entity
@Sql
public class Aggregate
{
	public DateTime date;

	public long millis;

	public String key;

	public int value;

	public Aggregate(DateTime date, int value)
	{
		this.date = date;
		this.value = value;
	}

	public Aggregate(long millis, String key, int value)
	{
		this.millis = millis;
		this.key = key;
		this.value = value;
	}
}
