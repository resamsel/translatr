import { FeatureFlagDto } from '../generated/model/featureFlagDto';
import { Feature } from './feature';

export interface UserFeatureFlag extends Omit<FeatureFlagDto, 'feature'> {
  feature: Feature;
}
