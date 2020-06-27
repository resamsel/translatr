import { Feature, Member, Setting, Temporal, UserRole } from '.';

export interface User extends Temporal {
  id?: string;

  name: string;
  username: string;
  email?: string;
  emailHash?: string;
  role?: UserRole;
  preferredLanguage?: string;

  memberships?: Member[];

  features?: Record<Feature, boolean>;
  settings?: Record<Setting, string>;
}
