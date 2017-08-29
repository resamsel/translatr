package models;

import com.avaje.ebean.annotation.Sql;
import java.util.UUID;
import javax.persistence.Entity;

/**
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
