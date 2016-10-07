# --- !Ups

alter table project
	add constraint uq_project_owner_id_name unique(owner_id, name);

# --- !Downs

alter table project
	drop constraint uq_project_owner_id_name;
