package models;

import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.avaje.ebean.Model.Find;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Locale {
	public Locale() {
	}

	public Locale(Project project, String name) {
		this.project = project;
		this.name = name;
	}

	@Id
	public UUID id;

	@ManyToOne(fetch = FetchType.EAGER)
	public Project project;

	@Column(length = 15)
	public String name;

	@JsonIgnore
	@OneToMany
	public List<Message> messages;

	public static final Find<UUID, Locale> find = new Find<UUID, Locale>() {
	};
}
