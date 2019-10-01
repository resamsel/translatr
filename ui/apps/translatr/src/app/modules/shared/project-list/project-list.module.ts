import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProjectListComponent } from './project-list.component';
import { RouterModule } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatToolbarModule } from '@angular/material/toolbar';
import { NavListModule } from '../nav-list/nav-list.module';
import { MomentModule } from 'ngx-moment';
import { MatTooltipModule } from '@angular/material';

@NgModule({
  declarations: [ProjectListComponent],
  imports: [
    CommonModule,
    RouterModule,
    NavListModule,
    MatButtonModule,
    MatIconModule,
    MatToolbarModule,
    MatListModule,
    MomentModule,
    MatTooltipModule
  ],
  exports: [ProjectListComponent]
})
export class ProjectListModule {}
