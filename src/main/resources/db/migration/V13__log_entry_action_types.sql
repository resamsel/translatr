alter table user_ drop column last_login;
alter table log_entry
	drop constraint ck_log_entry_type,
	add constraint ck_log_entry_type
		check (type in ('Create','Update','Delete','Login','Logout'));
