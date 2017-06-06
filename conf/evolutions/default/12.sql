# --- !Ups

update project_user
	set role = 'Manager'
	from project p
	where p.id = project_id
		and role = 'Owner'
		and p.owner_id != user_id;

# --- !Downs

update project_user
	set role = 'Owner'
	where role = 'Manager';
