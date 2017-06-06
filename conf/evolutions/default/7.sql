# --- !Ups

update user_
	set username = replace(replace(email, '@', ''), '.', '')
	where username is null and email is not null;
alter table user_
	alter column email drop not null;
create unique index ix_user_username on user_ (username);

alter table project
	alter column owner_id drop not null;
alter table project_user
	alter column project_id set not null;
alter table project_user
	alter column user_id set not null;
alter table project_user
	drop constraint fk_project_user_user_id;
alter table project_user
	add constraint fk_project_user_user_id
		foreign key (user_id)
		references user_ (id)
		on delete cascade
		on update restrict;
alter table linked_account
	alter column user_id set not null;
alter table linked_account
	alter column provider_user_id set not null;
alter table linked_account
	alter column provider_key set not null;
alter table linked_account
	drop constraint fk_linked_account_user_id;
alter table linked_account
	add constraint fk_linked_account_user_id
		foreign key (user_id)
		references user_ (id)
		on delete cascade
		on update restrict;
alter table log_entry
	alter column user_id drop not null;

# --- !Downs

drop index ix_user_username;
alter table user_
	alter column email set not null;
delete from project where owner_id is null;
alter table project
	alter column owner_id set not null;
alter table project_user
	alter column project_id drop not null;
alter table project_user
	alter column user_id drop not null;
alter table linked_account
	alter column user_id drop not null;
alter table linked_account
	alter column provider_user_id drop not null;
alter table linked_account
	alter column provider_key drop not null;
alter table log_entry
	alter column user_id set not null;
