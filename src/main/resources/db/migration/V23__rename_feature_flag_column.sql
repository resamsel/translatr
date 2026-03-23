drop index ix_user_feature_flag_user_id_name;
alter table user_feature_flag
    rename column feature_flag to feature;

create unique index ix_user_feature_flag_user_id_name
    on user_feature_flag (user_id, feature);
