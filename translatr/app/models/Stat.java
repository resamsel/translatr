package models;

import java.util.UUID;

import javax.persistence.Entity;

import com.avaje.ebean.annotation.Sql;

/**
 * 
 * <p>
 *
 * @author resamsel
 * @version 2 Sep 2016
 */
@Entity
@Sql
public class Stat
{
	public UUID id;

	public Long count;
}
