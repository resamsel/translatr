import { RequestCriteria } from '@dev/translatr-model';

export interface ActivityCriteria extends RequestCriteria {
  userId?: string;
  projectId?: string;
  projectOwnerId?: string;
}
