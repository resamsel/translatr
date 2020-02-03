import { RequestCriteria } from './request-criteria';

export interface MemberCriteria extends RequestCriteria {
  projectId?: string;
  roles?: string;
}
