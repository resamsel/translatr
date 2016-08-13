package models;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.avaje.ebean.Model.Find;

@Entity
public class Message {
	@Id
	public UUID id;

	@ManyToOne(fetch = FetchType.EAGER)
	public Locale locale;

	@ManyToOne(fetch = FetchType.EAGER)
	public Key key;
	
	@Column(length = 20 * 1024)
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
