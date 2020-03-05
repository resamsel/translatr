import { Feature, UserEmbedded } from '@translatr/translatr-model/src';

export interface UserFeatureFlag extends UserEmbedded {
  id: string;
  feature: Feature;
  enabled: boolean;
}
