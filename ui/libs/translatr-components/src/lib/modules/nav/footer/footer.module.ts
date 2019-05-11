import { NgModule } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FooterComponent } from "./footer.component";
import { MatIconModule } from "@angular/material";
import { RouterModule } from "@angular/router";

@NgModule({
  declarations: [FooterComponent],
  imports: [
    CommonModule,
    RouterModule,
    MatIconModule
  ],
  exports: [FooterComponent]
})
export class FooterModule {
}
