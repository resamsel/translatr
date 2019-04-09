import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MainPageComponent } from './main-page.component';
import { MatButtonModule, MatCardModule, MatGridListModule, MatIconModule, MatMenuModule } from '@angular/material';
import { LayoutModule } from '@angular/cdk/layout';
import { MainPageRoutingModule } from "./main-page-routing.module";
import { NavbarModule } from "@dev/translatr-components/src/lib";

@NgModule({
  declarations: [MainPageComponent],
  imports: [
    MainPageRoutingModule,
    CommonModule,
    NavbarModule,
    MatGridListModule,
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    MatMenuModule,
    LayoutModule
  ]
})
export class MainPageModule {
}
