import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {NavbarComponent} from './navbar.component';
import { MatButtonModule, MatDividerModule, MatIconModule, MatMenuModule, MatToolbarModule } from "@angular/material";
import {AuthBarItemComponent} from "./auth-bar-item/auth-bar-item.component";
import {RouterModule} from "@angular/router";
import {GravatarModule} from "ngx-gravatar";

@NgModule({
  declarations: [NavbarComponent, AuthBarItemComponent],
  imports: [
    CommonModule,
    MatIconModule,
    MatToolbarModule,
    MatButtonModule,
    MatMenuModule,
    MatDividerModule,
    RouterModule,
    GravatarModule
  ],
  exports: [
    NavbarComponent
  ]
})
export class NavbarModule {
}
