import { MemberDto } from '../generated/model/memberDto';
import { MemberRole } from './member-role';

export interface Member extends Omit<MemberDto, 'role'> {
  role: MemberRole;

  projectOwnerUsername?: string;
}
