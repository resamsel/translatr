# --- !Ups

create index ix_log_entry_type on log_entry (type);
create index ix_log_entry_when_created on log_entry (when_created);

# --- !Downs

drop index ix_log_entry_type;
drop index ix_log_entry_when_created;
