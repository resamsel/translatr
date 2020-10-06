import { RequestCriteria } from './request-criteria';
import { UserRole } from './user-role';

export interface UserCriteria extends RequestCriteria {
  userId?: string;
  role?: UserRole;
}
