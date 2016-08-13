# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table key (
  id                        varchar(40) not null,
  project_id                varchar(40),
  name                      varchar(255),
  constraint uq_key_1 unique (project_id,name),
  constraint pk_key primary key (id))
;

create table locale (
  id                        varchar(40) not null,
  project_id                varchar(40),
  name                      varchar(15),
  constraint pk_locale primary key (id))
;

create table message (
  id                        varchar(40) not null,
  locale_id                 varchar(40),
  key_id                    varchar(40),
  value                     varchar(20480),
  constraint pk_message primary key (id))
;

create table project (
  id                        varchar(40) not null,
  name                      varchar(255),
  constraint pk_project primary key (id))
;

alter table key add constraint fk_key_project_1 foreign key (project_id) references project (id) on delete restrict on update restrict;
create index ix_key_project_1 on key (project_id);
alter table locale add constraint fk_locale_project_2 foreign key (project_id) references project (id) on delete restrict on update restrict;
create index ix_locale_project_2 on locale (project_id);
alter table message add constraint fk_message_locale_3 foreign key (locale_id) references locale (id) on delete restrict on update restrict;
create index ix_message_locale_3 on message (locale_id);
alter table message add constraint fk_message_key_4 foreign key (key_id) references key (id) on delete restrict on update restrict;
create index ix_message_key_4 on message (key_id);



# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists key;

drop table if exists locale;

drop table if exists message;

drop table if exists project;

SET REFERENTIAL_INTEGRITY TRUE;

