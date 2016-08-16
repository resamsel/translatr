# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table key (
  id                        varchar(40) not null,
  project_id                varchar(40) not null,
  name                      varchar(255),
  when_created              timestamp not null,
  when_updated              timestamp not null,
  constraint uq_key_1 unique (project_id,name),
  constraint pk_key primary key (id))
;

create table locale (
  id                        varchar(40) not null,
  project_id                varchar(40) not null,
  name                      varchar(15) not null,
  when_created              timestamp not null,
  when_updated              timestamp not null,
  constraint uq_locale_1 unique (project_id,name),
  constraint pk_locale primary key (id))
;

create table message (
  id                        varchar(40) not null,
  locale_id                 varchar(40) not null,
  key_id                    varchar(40) not null,
  value                     varchar(20480) not null,
  when_created              timestamp not null,
  when_updated              timestamp not null,
  constraint uq_message_1 unique (locale_id,key_id),
  constraint pk_message primary key (id))
;

create table project (
  id                        varchar(40) not null,
  name                      varchar(255) not null,
  when_created              timestamp not null,
  when_updated              timestamp not null,
  constraint uq_project_1 unique (name),
  constraint pk_project primary key (id))
;

alter table key add constraint fk_key_project_1 foreign key (project_id) references project (id);
create index ix_key_project_1 on key (project_id);
alter table locale add constraint fk_locale_project_2 foreign key (project_id) references project (id);
create index ix_locale_project_2 on locale (project_id);
alter table message add constraint fk_message_locale_3 foreign key (locale_id) references locale (id);
create index ix_message_locale_3 on message (locale_id);
alter table message add constraint fk_message_key_4 foreign key (key_id) references key (id);
create index ix_message_key_4 on message (key_id);



# --- !Downs

drop table if exists key cascade;

drop table if exists locale cascade;

drop table if exists message cascade;

drop table if exists project cascade;

