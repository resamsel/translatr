# --- !Ups

alter table project
    drop constraint fk_project_owner_id,
    add constraint fk_project_owner_id
        foreign key (owner_id)
            references user_ (id)
            on update restrict
            on delete cascade;

# --- !Downs

alter table project
    drop constraint fk_project_owner_id,
    add constraint fk_project_owner_id
        foreign key (owner_id)
            references user_ (id)
            on update restrict
            on delete set null;
