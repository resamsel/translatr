import { Member, Temporal, UserRole } from '.';

export interface User extends Temporal {
  id?: string;

  name: string;
  username: string;
  email?: string;
  role?: UserRole;

  memberships?: Member[];
}
