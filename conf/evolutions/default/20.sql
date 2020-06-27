# --- !Ups

delete
from project_user using (
    select project_id,
           user_id,
           unnest(array_remove(array_agg(id), min(id))) project_user_id
    from project_user
    group by 1, 2
    having count(*) > 1
) x
where project_user.id = x.project_user_id;

create unique index ix_project_user_project_id_user_id
    on project_user (project_id, user_id);

# --- !Downs

drop index ix_project_user_project_id_user_id;
