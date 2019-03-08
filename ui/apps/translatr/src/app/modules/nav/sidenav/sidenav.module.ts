import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule, MatListModule, MatSidenavModule, MatToolbarModule} from "@angular/material";
import {SidenavComponent} from "./sidenav.component";
import {NavbarModule} from "../navbar/navbar.module";
import { FooterModule } from "../footer/footer.module";
import { RouterModule } from "@angular/router";

@NgModule({
  declarations: [SidenavComponent],
  imports: [
    CommonModule,
    RouterModule,
    FooterModule,
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
