# --- !Ups

delete from project where owner_id is null;
alter table project
  alter column owner_id set not null;
alter table project
  add column description varchar(2000);

# --- !Downs

alter table project
  alter column owner_id drop not null;
alter table project
  drop column description;
