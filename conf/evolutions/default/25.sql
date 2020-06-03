# --- !Ups

alter table user_
    add column settings jsonb;

# --- !Downs

alter table user_
    drop column settings;
