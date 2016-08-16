package models;

import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.joda.time.DateTime;

import com.avaje.ebean.Model.Find;
import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "locale_id", "key_id" }) })
public class Message {
	@Id
	public UUID id;
	
	@CreatedTimestamp
	public DateTime whenCreated;
	
	@UpdatedTimestamp
	public DateTime whenUpdated;

	@ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
	public Locale locale;

	@ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
	public Key key;

	@Column(nullable = false, length = 20 * 1024)
	public String value;

	public Message() {
	}

	public Message(Locale locale, Key key, String value) {
		this.locale = locale;
		this.key = key;
		this.value = value;
	}

	public static final Find<UUID, Message> find = new Find<UUID, Message>() {
	};

	public void updateFrom(Message in) {
		value = in.value;
	}
}
