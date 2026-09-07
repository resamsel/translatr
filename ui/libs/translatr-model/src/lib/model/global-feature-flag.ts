import { GlobalFeatureFlagDto } from '../generated/model/globalFeatureFlagDto';
import { Feature } from './feature';

export interface GlobalFeatureFlag extends Omit<GlobalFeatureFlagDto, 'feature'> {
  feature: Feature;
}
