import { UserRole } from '@dev/translatr-model';
import { RequestCriteria } from './request-criteria';

export interface AccessTokenCriteria extends RequestCriteria {
  userId?: string;
  userRole?: UserRole;
}
