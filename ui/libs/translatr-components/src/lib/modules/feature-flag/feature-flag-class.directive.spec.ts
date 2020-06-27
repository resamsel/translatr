import { FeatureFlagClassDirective } from './feature-flag-class.directive';

describe('FeatureFlagClassDirective', () => {
  it('should create an instance', () => {
    const directive = new FeatureFlagClassDirective(undefined, undefined, undefined);
    expect(directive).toBeTruthy();
  });
});
