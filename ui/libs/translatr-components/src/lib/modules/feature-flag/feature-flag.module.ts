import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FeatureFlagDirective } from './feature-flag.directive';
import { FeatureFlagClassDirective } from './feature-flag-class.directive';

@NgModule({
  declarations: [FeatureFlagDirective, FeatureFlagClassDirective],
  imports: [CommonModule],
  exports: [FeatureFlagDirective, FeatureFlagClassDirective]
})
export class FeatureFlagModule {
}
