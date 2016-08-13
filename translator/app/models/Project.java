package models;

import java.util.List;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.avaje.ebean.Model.Find;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Project {
	public Project() {
	}

	public Project(String name) {
		this.name = name;
	}

	@Id
	public UUID id;

	public String name;

	@JsonIgnore
	@OneToMany
	public List<Locale> locales;

	@JsonIgnore
	@OneToMany
	public List<Key> keys;

	public static final Find<UUID, Project> find = new Find<UUID, Project>() {
	};

	public float progress(Locale locale) {
		if (keys.size() > 0)
			return (float) locale.messages.size() / (float) keys.size();
		return 0f;
	}

	public int messagesSize() {
		return Message.find.where().eq("key.project", this).findRowCount();
	}
}
