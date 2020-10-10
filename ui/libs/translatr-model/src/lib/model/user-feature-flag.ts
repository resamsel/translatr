import { Feature } from './feature';
import { UserEmbedded } from './user-embedded';

export interface UserFeatureFlag extends UserEmbedded {
  id: string;
  feature: Feature;
  enabled: boolean;
}
