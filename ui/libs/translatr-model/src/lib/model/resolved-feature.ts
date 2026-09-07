import { ResolvedFeatureDto } from '../generated/model/resolvedFeatureDto';
import { Feature } from './feature';

export interface ResolvedFeature extends Omit<ResolvedFeatureDto, 'feature'> {
  feature: Feature;
}
