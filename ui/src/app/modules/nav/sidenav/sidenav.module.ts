import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule, MatListModule, MatSidenavModule, MatToolbarModule} from "@angular/material";
import {SidenavComponent} from "./sidenav.component";
import {NavbarModule} from "../navbar/navbar.module";

@NgModule({
  declarations: [SidenavComponent],
  imports: [
    CommonModule,
    NavbarModule,
    MatSidenavModule,
    MatToolbarModule,
    MatIconModule,
    MatListModule
  ],
  exports: [SidenavComponent]
})
export class SidenavModule {
}
