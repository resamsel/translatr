# --- !Ups

create table user_ (
  id                            uuid not null,
  version                       bigint not null default 0,
  deleted                       boolean not null default false,
  when_created                  timestamp not null,
  when_updated                  timestamp not null,
  username                      varchar(32) not null,
  name                          varchar(32) not null,
  email                         varchar(255) not null,
  avatar_url                    varchar(255),
  constraint uq_user_username unique (username),
  constraint uq_user_email unique (email),
  constraint pk_user primary key (id)
);

insert into user_
	(id, when_created, when_updated, username, name, email, avatar_url)
	values
	('1258D7C1-A1B5-40B0-A79B-0E8B64C7560A', now(), now(), 'resamsel', 'René Samselnig', 'rene.samselnig@gmail.com', 'https://avatars3.githubusercontent.com/u/3007332');

alter table project
	add column owner_id uuid;

update project set owner_id = '1258D7C1-A1B5-40B0-A79B-0E8B64C7560A';
	
alter table project
	add constraint fk_project_owner_id
		foreign key (owner_id)
		references user_ (id)
		on delete set null
		on update restrict;
create index ix_project_owner_id on project (owner_id);

alter table project
	alter column owner_id set not null;

alter table log_entry
	add column user_id uuid;

update log_entry set user_id = '1258D7C1-A1B5-40B0-A79B-0E8B64C7560A';

alter table log_entry
	add constraint fk_log_entry_user_id
		foreign key (user_id)
		references user_ (id)
		on delete set null
		on update restrict;
create index ix_log_entry_user_id on log_entry (user_id);

alter table log_entry
	alter column user_id set not null;

alter table log_entry
	alter column project_id drop not null;

# --- !Downs

alter table log_entry drop column user_id;
delete from log_entry where project_id is null;
alter table log_entry
	alter column project_id set not null;
alter table project drop column owner_id;
drop table user_;
