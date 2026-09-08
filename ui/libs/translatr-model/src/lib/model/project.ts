import { ProjectDto } from '../generated/model/projectDto';
import { Member } from './member';
import { MemberRole } from './member-role';

export interface Project extends Omit<ProjectDto, 'members' | 'myRole'> {
  members?: Member[];
  myRole?: MemberRole;
}
