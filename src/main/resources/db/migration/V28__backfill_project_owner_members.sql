-- The owner is always a member of their project. Projects created by the Quarkus
-- backend before ProjectService.create started writing the owner ProjectUser row
-- are missing it — backfill them (mirrors the Play-era conf/evolutions/default/5.sql).
insert into project_user (id, when_created, when_updated, project_id, user_id, role)
select nextval('project_user_id_seq'), now(), now(), p.id, p.owner_id, 'Owner'
from project p
where p.owner_id is not null
  and not exists (
    select 1 from project_user pu
    where pu.project_id = p.id
      and pu.user_id = p.owner_id
  );
