package models;

import io.ebean.annotation.Sql;

import javax.persistence.Entity;
import java.util.UUID;

/**
 *
 * @author resamsel
 * @version 2 Sep 2016
 */
@Entity
@Sql
public class Stat {
	public UUID id;

	public Double count;
}
