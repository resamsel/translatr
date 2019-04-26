import { RequestCriteria } from './request-criteria';

export interface KeyCriteria extends RequestCriteria {
  projectId?: string;
}
