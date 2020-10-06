import { UserRole } from '@translatr/translatr-model/src';
import { RequestCriteria } from './request-criteria';

export interface AccessTokenCriteria extends RequestCriteria {
  userId?: string;
  userRole?: UserRole;
}
