import { RequestCriteria } from './request-criteria';

export interface FeatureFlagCriteria extends RequestCriteria {
  userId?: string;
}
