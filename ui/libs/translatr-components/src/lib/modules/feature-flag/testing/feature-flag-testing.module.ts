import { Directive, Input, NgModule } from '@angular/core';
import { Feature } from '@dev/translatr-model';

@Directive({
  selector: '[featureFlag]'
})
export class MockFeatureFlagDirective {
  @Input() featureFlag: Feature | Feature[];
}

@Directive({
  selector: '[featureFlagClass]'
})
export class MockFeatureFlagClassDirective {
  @Input() featureFlagClass: Record<string, Feature>;
}

@NgModule({
  declarations: [MockFeatureFlagDirective, MockFeatureFlagClassDirective],
  exports: [MockFeatureFlagDirective, MockFeatureFlagClassDirective]
})
export class FeatureFlagTestingModule {
}
