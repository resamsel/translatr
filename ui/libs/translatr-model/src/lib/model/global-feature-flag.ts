import { Feature } from './feature';
import { Temporal } from './temporal';

export interface GlobalFeatureFlag extends Temporal {
  id?: string;
  feature: Feature;
  enabled: boolean;
}
