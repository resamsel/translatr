# --- !Ups

alter table user_
    add column preferred_locale varchar(16);

# --- !Downs

alter table user_
    drop column preferred_locale;
