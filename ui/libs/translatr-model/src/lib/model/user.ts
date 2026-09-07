import { UserDto } from '../generated/model/userDto';
import { Feature, Member, Setting, UserRole } from '.';

export interface User
  extends Omit<
    UserDto,
    'whenCreated' | 'whenUpdated' | 'name' | 'username' | 'role' | 'features' | 'settings'
  > {
  whenCreated?: Date;
  whenUpdated?: Date;

  name: string;
  username: string;
  role?: UserRole;
  preferredLanguage?: string;

  memberships?: Member[];

  features?: Record<Feature, boolean>;
  settings?: Record<Setting, string>;
}
