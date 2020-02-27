import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FeatureFlagDirective} from "./feature-flag.directive";

@NgModule({
  declarations: [FeatureFlagDirective],
  imports: [CommonModule],
  exports: [FeatureFlagDirective]
})
export class FeatureFlagModule {
}
