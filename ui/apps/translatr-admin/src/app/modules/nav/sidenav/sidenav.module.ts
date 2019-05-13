import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatButtonModule, MatIconModule, MatListModule, MatMenuModule, MatSidenavModule, MatToolbarModule} from '@angular/material';
import {SidenavComponent} from './sidenav.component';
import {RouterModule} from '@angular/router';
import {FooterModule, NavbarModule} from '@dev/translatr-components';

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
    MatButtonModule,
    MatListModule,
    MatMenuModule
  ],
  exports: [SidenavComponent]
})
export class SidenavModule {
}
