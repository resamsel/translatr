import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { RouterModule } from '@angular/router';
import { SidenavModule } from '../nav/sidenav/sidenav.module';
import { AdminPageComponent } from './admin-page.component';
import { MatIconButton } from '@angular/material/button';

@NgModule({
  declarations: [AdminPageComponent],
  imports: [
    CommonModule,
    RouterModule,
    SidenavModule,

    MatSidenavModule,
    MatToolbarModule,
    MatIconModule,
    MatListModule,
    MatIconButton,
  ],
  exports: [AdminPageComponent],
})
export class AdminPageModule {}
