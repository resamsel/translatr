import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { RouterModule } from '@angular/router';
import { ErrorPageComponent, ErrorPageHeaderComponent, ErrorPageMessageComponent } from '@dev/translatr-components';
import { SidenavModule } from '../../nav/sidenav/sidenav.module';
import { ForbiddenPageComponent } from './forbidden-page.component';

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
    ErrorPageComponent, ErrorPageHeaderComponent, ErrorPageMessageComponent
  ]
})
export class ForbiddenPageModule {}
