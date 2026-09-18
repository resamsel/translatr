import { Directive, Input } from '@angular/core';
import { Feature } from '@dev/translatr-model';

@Directive({
  standalone: true,
  selector: '[featureFlag]'
})
export class MockFeatureFlagDirective {
  @Input() featureFlag: Feature | Feature[];
}

@Directive({
  standalone: true,
  selector: '[featureFlagClass]'
})
export class MockFeatureFlagClassDirective {
  @Input() featureFlagClass: Record<string, Feature>;
}
