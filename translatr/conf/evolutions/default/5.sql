# --- !Ups

insert into project_user (project_id, user_id, role, when_created, when_updated)
	select p.id, p.owner_id, 'Owner', now(), now()
		from project p;

# --- !Downs

delete from project_user where role = 'Owner';
