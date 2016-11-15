# --- !Ups

alter table message
	drop constraint fk_message_key_id;
alter table message
	add constraint fk_message_key_id
		foreign key (key_id)
		references key (id)
		on delete cascade
		on update restrict;
alter table message
	drop constraint fk_message_locale_id;
alter table message
	add constraint fk_message_locale_id
		foreign key (locale_id)
		references locale (id)
		on delete cascade
		on update restrict;
alter table key
	drop constraint fk_key_project_id;
alter table key
	add constraint fk_key_project_id
		foreign key (project_id)
		references project (id)
		on delete cascade
		on update restrict;
alter table locale
	drop constraint fk_locale_project_id;
alter table locale
	add constraint fk_locale_project_id
		foreign key (project_id)
		references project (id)
		on delete cascade
		on update restrict;
alter table log_entry
	drop constraint fk_log_entry_project_id;
alter table log_entry
	add constraint fk_log_entry_project_id
		foreign key (project_id)
		references project (id)
		on delete cascade
		on update restrict;
alter table project_user
	drop constraint fk_project_user_project_id;
alter table project_user
	add constraint fk_project_user_project_id
		foreign key (project_id)
		references project (id)
		on delete cascade
		on update restrict;

# --- !Downs

