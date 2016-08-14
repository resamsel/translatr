package models;

import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.avaje.ebean.Model.Find;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "project_id", "name" }) })
public class Locale {
	@Id
	public UUID id;

	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	public Project project;

	@Column(nullable = false, length = 15)
	public String name;

	@JsonIgnore
	@OneToMany
	public List<Message> messages;

	public Locale() {
	}
	
	public Locale(Project project, String name) {
		this.project = project;
		this.name = name;
	}
	
	public static final Find<UUID, Locale> find = new Find<UUID, Locale>() {
	};
}
