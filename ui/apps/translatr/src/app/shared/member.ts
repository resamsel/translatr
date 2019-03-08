import {MemberRole} from "./member-role";

export interface Member {
  id: string;
  userName: string;
  userId: string;
  userUsername: string;
  role: MemberRole
}
