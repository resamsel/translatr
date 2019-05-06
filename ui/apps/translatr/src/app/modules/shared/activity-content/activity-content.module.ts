import { NgModule } from "@angular/core";
import { CommonModule } from "@angular/common";
import { ActivityContentComponent } from "./activity-content.component";
import { RouterModule } from "@angular/router";

@NgModule({
  declarations: [ActivityContentComponent],
  exports: [ActivityContentComponent],
  imports: [
    CommonModule,
    RouterModule
  ]
})
export class ActivityContentModule { }
