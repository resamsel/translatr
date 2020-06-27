import { FeatureFlagDirective } from './feature-flag.directive';

describe('FeatureFlagDirective', () => {
  it('should create an instance', () => {
    const directive = new FeatureFlagDirective(undefined, undefined, undefined);
    expect(directive).toBeTruthy();
  });
});
