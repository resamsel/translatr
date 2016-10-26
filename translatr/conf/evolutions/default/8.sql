# --- !Ups

delete from linked_account
	where id in (
		select id
			from (
				select id,
						row_number() over (
							partition by provider_user_id, provider_key
							order by when_created) as rnum
					from linked_account
			) t
			where t.rnum > 1);
create unique index ix_linked_account_provider_user_id_provider_key
	on linked_account (provider_user_id, provider_key);

# --- !Downs

drop index ix_linked_account_provider_user_id_provider_key;
