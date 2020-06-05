import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FeatureFlagClassDirective } from './feature-flag-class.directive';
import { FeatureFlagDirective } from './feature-flag.directive';

@NgModule({
  declarations: [FeatureFlagDirective, FeatureFlagClassDirective],
  imports: [CommonModule],
  exports: [FeatureFlagDirective, FeatureFlagClassDirective]
})
export class FeatureFlagModule {}
