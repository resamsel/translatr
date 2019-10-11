import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ForbiddenPageComponent } from './forbidden-page.component';
import { SidenavModule } from '../../nav/sidenav/sidenav.module';
import { MatButtonModule, MatCardModule, MatDividerModule, MatIconModule } from '@angular/material';
import { RouterModule } from '@angular/router';
import { ErrorPageModule } from '@dev/translatr-components';

@NgModule({
  declarations: [ForbiddenPageComponent],
  imports: [
    CommonModule,
    SidenavModule,
    MatCardModule,
    MatDividerModule,
    MatButtonModule,
    RouterModule,
    MatIconModule,
    ErrorPageModule
  ]
})
export class ForbiddenPageModule {
}
