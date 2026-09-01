import { Feature } from './feature';

export interface ResolvedFeature {
  feature: Feature;
  defaultEnabled: boolean;
  global: boolean | null;
  userOverride: boolean | null;
  userOverrideId: string | null;
  effective: boolean;
}
