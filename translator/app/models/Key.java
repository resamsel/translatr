package models;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.avaje.ebean.Model.Find;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "project_id", "name" }) })
public class Key {
	@Id
	public UUID id;

	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	public Project project;

	@Column(length = 255)
	public String name;

	public Key() {
	}
	
	public Key(Project project, String name) {
		this.project = project;
		this.name = name;
	}
	
	public static final Find<UUID, Key> find = new Find<UUID, Key>() {
	};

	public String messageValue(Locale locale) {
		Message message = Message.find.where().eq("key", this).eq("locale", locale).findUnique();
		if (message == null)
			return null;
		return message.value;
	}
}
