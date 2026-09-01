create table feature_flag (
  id                            uuid not null,
  when_created                  timestamp not null,
  when_updated                  timestamp not null,
  feature                       varchar(64) not null,
  enabled                       boolean not null,
  constraint pk_feature_flag primary key (id)
);

create unique index ix_feature_flag_feature on feature_flag (feature);
