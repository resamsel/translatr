import { RequestCriteria } from './request-criteria';

export interface MessageCriteria extends RequestCriteria {
  projectId?: string;
}
