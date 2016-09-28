# --- !Ups

alter table key alter column name set not null;

# --- !Downs

alter table key alter column name drop not null;
