# --- !Ups

--delete from linked_account;
create unique index ix_linked_account_provider_user_id_provider_key
	on linked_account (provider_user_id, provider_key);

# --- !Downs

drop index ix_linked_account_provider_user_id_provider_key;
