package models;

import java.util.List;
import java.util.UUID;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.annotation.WillCloseWhenClosed;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.joda.time.DateTime;

import com.avaje.ebean.Model.Find;
import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }) })
public class Project {
	@Id
	public UUID id;
	
	@CreatedTimestamp
	public DateTime whenCreated;
	
	@UpdatedTimestamp
	public DateTime whenUpdated;

	@Column(nullable = false)
	public String name;

	@JsonIgnore
	@OneToMany
	public List<Locale> locales;

	@JsonIgnore
	@OneToMany
	public List<Key> keys;

	public Project() {
	}
	
	public Project(String name) {
		this.name = name;
	}
	
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
