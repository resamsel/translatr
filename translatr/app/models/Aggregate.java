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

	public int value;

	public Aggregate(long millis, int value)
	{
		this.millis = millis;
		this.value = value;
	}
}
