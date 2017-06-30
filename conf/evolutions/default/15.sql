# --- !Ups

alter table project add column path varchar(255);

update project set path = left(lower(regexp_replace(name, '[^a-zA-Z0-9_-]', '-')), 255);

alter table project
	add constraint uq_project_owner_id_path unique(owner_id, path);

-- TODO: clean up duplicates

alter table project alter column path set not null;

# --- !Downs

alter table project drop column path;
