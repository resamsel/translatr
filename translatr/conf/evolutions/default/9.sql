# --- !Ups

create table access_token (
  id                            bigserial not null,
  version                       bigint not null default 0,
  when_created                  timestamp not null,
  when_updated                  timestamp not null,
  user_id                       uuid not null,
  name                          varchar(32) not null,
  key                           varchar(64) not null,
  scope                         varchar(255),
  constraint pk_access_token primary key (id)
);

alter table access_token
	add constraint fk_access_token_user_id
		foreign key (user_id)
		references user_ (id)
		on delete cascade
		on update restrict;
create index ix_access_token_user_id on access_token (user_id);
create unique index ix_access_token_key on access_token (key);
create unique index ix_access_token_user_id_name on access_token (user_id, name);

# --- !Downs

drop table access_token;
