import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule, MatListModule, MatSidenavModule, MatToolbarModule} from "@angular/material";
import {SidenavComponent} from "./sidenav.component";
import {NavbarModule} from "../navbar/navbar.module";
import { FooterModule } from "../footer/footer.module";
import { RouterModule } from "@angular/router";
import { SearchBarComponent } from "../navbar/search-bar/search-bar.component";

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
  exports: [SidenavComponent, SearchBarComponent]
})
export class SidenavModule {
}
