# --- !Ups

alter table user_ drop column last_login;
alter table log_entry
	drop constraint ck_log_entry_type,
	add constraint ck_log_entry_type
		check (type in ('Create','Update','Delete','Login','Logout'));

# --- !Downs

alter table user_ add column last_login timestamp;
delete from log_entry where type in ('Login','Logout');
alter table log_entry
	drop constraint ck_log_entry_type,
	add constraint ck_log_entry_type
		check (type in ('Create','Update','Delete'));
