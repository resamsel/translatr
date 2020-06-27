# --- !Ups

create table user_feature_flag (
  id                            uuid not null,
  when_created                  timestamp not null,
  when_updated                  timestamp not null,
  user_id                       uuid not null,
  feature_flag                  varchar(64) not null,
  enabled                       boolean,
  constraint pk_user_feature_flag primary key (id)
);

alter table user_feature_flag
	add constraint fk_user_feature_flag_user_id
		foreign key (user_id)
		references user_ (id)
		on delete cascade
		on update restrict;
create index ix_user_feature_flag_user_id on user_feature_flag (user_id);
create unique index ix_user_feature_flag_user_id_name on user_feature_flag (user_id, feature_flag);

# --- !Downs

drop table user_feature_flag;
