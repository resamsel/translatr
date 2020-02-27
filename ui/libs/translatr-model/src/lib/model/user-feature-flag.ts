import { FeatureFlag, UserEmbedded } from '@translatr/translatr-model/src';

export interface UserFeatureFlag extends UserEmbedded {
  id: string;
  featureFlag: FeatureFlag;
  enabled: boolean;
}
