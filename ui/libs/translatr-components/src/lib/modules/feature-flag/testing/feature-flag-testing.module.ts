import { Directive, Input, NgModule } from '@angular/core';
import { Feature } from '@dev/translatr-model';

@Directive({
  standalone: false,
  selector: '[featureFlag]'
})
export class MockFeatureFlagDirective {
  @Input() featureFlag: Feature | Feature[];
}

@Directive({
  standalone: false,
  selector: '[featureFlagClass]'
})
export class MockFeatureFlagClassDirective {
  @Input() featureFlagClass: Record<string, Feature>;
}

@NgModule({
  declarations: [MockFeatureFlagDirective, MockFeatureFlagClassDirective],
  exports: [MockFeatureFlagDirective, MockFeatureFlagClassDirective]
})
export class FeatureFlagTestingModule {}
