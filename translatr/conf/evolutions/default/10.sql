# --- !Ups

alter table user_
	add column last_login timestamp;

# --- !Downs

alter table user_
	drop column last_login;
