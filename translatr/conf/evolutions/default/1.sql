# --- !Ups

create table key (
  id                            uuid not null,
  project_id                    uuid not null,
  name                          varchar(255),
  version                       bigint not null,
  when_created                  timestamp not null,
  when_updated                  timestamp not null,
  constraint uq_key_project_id_name unique (project_id,name),
  constraint pk_key primary key (id)
);

create table locale (
  id                            uuid not null,
  project_id                    uuid not null,
  name                          varchar(15) not null,
  version                       bigint not null,
  when_created                  timestamp not null,
  when_updated                  timestamp not null,
  constraint uq_locale_project_id_name unique (project_id,name),
  constraint pk_locale primary key (id)
);

create table log_entry (
  id                            uuid not null,
  type                          varchar(6) not null,
  content_type                  varchar(64) not null,
  project_id                    uuid not null,
  before                        varchar(1048576),
  after                         varchar(1048576),
  when_created                  timestamp not null,
  constraint ck_log_entry_type check (type in ('Create','Update','Delete')),
  constraint pk_log_entry primary key (id)
);

create table message (
  id                            uuid not null,
  locale_id                     uuid not null,
  key_id                        uuid not null,
  value                         varchar(1048576) not null,
  version                       bigint not null,
  when_created                  timestamp not null,
  when_updated                  timestamp not null,
  constraint uq_message_locale_id_key_id unique (locale_id,key_id),
  constraint pk_message primary key (id)
);

create table project (
  id                            uuid not null,
  deleted                       boolean,
  name                          varchar(255) not null,
  version                       bigint not null,
  when_created                  timestamp not null,
  when_updated                  timestamp not null,
  constraint uq_project_name unique (name),
  constraint pk_project primary key (id)
);

alter table key add constraint fk_key_project_id foreign key (project_id) references project (id) on delete restrict on update restrict;
create index ix_key_project_id on key (project_id);

alter table locale add constraint fk_locale_project_id foreign key (project_id) references project (id) on delete restrict on update restrict;
create index ix_locale_project_id on locale (project_id);

alter table log_entry add constraint fk_log_entry_project_id foreign key (project_id) references project (id) on delete restrict on update restrict;
create index ix_log_entry_project_id on log_entry (project_id);

alter table message add constraint fk_message_locale_id foreign key (locale_id) references locale (id) on delete restrict on update restrict;
create index ix_message_locale_id on message (locale_id);

alter table message add constraint fk_message_key_id foreign key (key_id) references key (id) on delete restrict on update restrict;
create index ix_message_key_id on message (key_id);

# --- !Downs

alter table if exists key drop constraint if exists fk_key_project_id;
drop index if exists ix_key_project_id;

alter table if exists locale drop constraint if exists fk_locale_project_id;
drop index if exists ix_locale_project_id;

alter table if exists log_entry drop constraint if exists fk_log_entry_project_id;
drop index if exists ix_log_entry_project_id;

alter table if exists message drop constraint if exists fk_message_locale_id;
drop index if exists ix_message_locale_id;

alter table if exists message drop constraint if exists fk_message_key_id;
drop index if exists ix_message_key_id;

drop table if exists key cascade;

drop table if exists locale cascade;

drop table if exists log_entry cascade;

drop table if exists message cascade;

drop table if exists project cascade;

