# --- !Ups

create index ix_user_role on user_ (role);

# --- !Downs

drop index ix_user_role;
