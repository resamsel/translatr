package models;

import java.util.UUID;

import javax.persistence.Entity;

import com.avaje.ebean.annotation.Sql;

/**
 * (c) 2016 Skiline Media GmbH
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
