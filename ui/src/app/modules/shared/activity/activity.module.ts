import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivityComponent } from "./activity.component";

@NgModule({
  declarations: [ActivityComponent],
  imports: [
    CommonModule
  ],
  exports: [ActivityComponent]
})
export class ActivityModule {
}
