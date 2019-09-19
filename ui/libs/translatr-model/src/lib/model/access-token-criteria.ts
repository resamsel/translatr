import { RequestCriteria } from './request-criteria';

export interface AccessTokenCriteria extends RequestCriteria {
  userId?: string;
}
