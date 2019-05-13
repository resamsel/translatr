import {MemberRole} from './member-role';

export interface Member {
  id: string;
  whenCreated: Date;
  whenUpdated: Date;

  role: MemberRole;

  userName: string;
  userId: string;
  userUsername: string;
  userEmail: string;
}
