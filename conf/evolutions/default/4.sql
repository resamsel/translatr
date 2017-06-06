# --- !Ups

create table project_user (
  id                            bigserial not null,
  version                       bigint not null default 0,
  when_created                  timestamp not null,
  when_updated                  timestamp not null,
  project_id                    uuid,
  user_id                       uuid,
  role                          varchar(16),
  constraint pk_project_user primary key (id)
);

alter table project_user
	add constraint fk_project_user_project_id
		foreign key (project_id)
		references project (id)
		on delete restrict
		on update restrict;
create index ix_project_user_project_id on project_user (project_id);
alter table project_user
	add constraint fk_project_user_user_id
		foreign key (user_id)
		references user_ (id)
		on delete restrict
		on update restrict;
create index ix_project_user_user_id on project_user (user_id);

# --- !Downs

drop table project_user;
