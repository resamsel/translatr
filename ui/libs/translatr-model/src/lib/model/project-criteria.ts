import { RequestCriteria } from './request-criteria';

export interface ProjectCriteria extends RequestCriteria {
  ownerId?: string;
  owner?: string;
  memberId?: string;
}
