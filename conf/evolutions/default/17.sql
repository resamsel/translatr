# --- !Ups

alter table user_
  add column role varchar(16) not null default 'User';

update user_
  set role = 'Admin'
  where username = 'translatr';

# --- !Downs

alter table user_
  drop column role;
