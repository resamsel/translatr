import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PermissionDeniedPageComponent } from './permission-denied-page.component';
import { SidenavModule } from '../../nav/sidenav/sidenav.module';
import { MatButtonModule, MatCardModule, MatDividerModule } from '@angular/material';
import { RouterModule } from '@angular/router';

@NgModule({
  declarations: [PermissionDeniedPageComponent],
  imports: [
    CommonModule,
    SidenavModule,
    MatCardModule,
    MatDividerModule,
    MatButtonModule,
    RouterModule
  ]
})
export class PermissionDeniedPageModule {
}
