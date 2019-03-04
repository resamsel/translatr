import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MainPageComponent} from './main-page.component';
import {MatButtonModule, MatCardModule, MatGridListModule, MatIconModule, MatMenuModule} from '@angular/material';
import {LayoutModule} from '@angular/cdk/layout';
import {MainPageRoutingModule} from "./main-page-routing.module";
import {RouterModule, Routes} from "@angular/router";
import {NavbarModule} from "../../nav/navbar/navbar.module";

const routes: Routes = [
  {path: '', component: MainPageComponent}
];

@NgModule({
  declarations: [MainPageComponent],
  imports: [
    RouterModule.forChild(routes),
    CommonModule,
    NavbarModule,
    MainPageRoutingModule,
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
