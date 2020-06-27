import { RequestCriteria } from './request-criteria';

export interface MessageCriteria extends RequestCriteria {
  projectId?: string;
  keyName?: string;
  keyIds?: string;
  localeId?: string;
  localeIds?: string;
}
