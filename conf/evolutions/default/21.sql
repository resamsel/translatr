# --- !Ups

alter table access_token
    drop column version;
alter table key
    drop column version;
alter table linked_account
    drop column version;
alter table locale
    drop column version;
alter table message
    drop column version;
alter table project
    drop column version;
alter table project_user
    drop column version;
alter table user_
    drop column version;

# --- !Downs

alter table access_token
    add column version bigint not null default 0;
alter table key
    add column version bigint not null default 0;
alter table linked_account
    add column version bigint not null default 0;
alter table locale
    add column version bigint not null default 0;
alter table message
    add column version bigint not null default 0;
alter table project
    add column version bigint not null default 0;
alter table project_user
    add column version bigint not null default 0;
alter table user_
    add column version bigint not null default 0;
