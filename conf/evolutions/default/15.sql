# --- !Ups

update user_ set username = md5(random()::text) where username is null;
alter table user_
  alter column username set not null;

# --- !Downs

alter table user_
  alter column username drop not null;
