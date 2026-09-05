import { ProjectPayload } from '../generated/model/projectPayload';
import { Member } from './member';
import { MemberRole } from './member-role';

export interface Project extends Omit<ProjectPayload, 'members' | 'myRole'> {
  members?: Member[];
  myRole?: MemberRole;
}
