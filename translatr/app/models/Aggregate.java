package models;

import javax.persistence.Entity;

import com.avaje.ebean.annotation.Sql;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 28 Aug 2016
 */
@Entity
@Sql
public class Aggregate
{
	public long millis;

	public String key;

	public int value;

	public Aggregate(long millis, String key, int value)
	{
		this.millis = millis;
		this.key = key;
		this.value = value;
	}
}
