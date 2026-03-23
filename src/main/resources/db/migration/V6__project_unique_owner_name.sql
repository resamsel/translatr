alter table project
	add constraint uq_project_owner_id_name unique(owner_id, name);
