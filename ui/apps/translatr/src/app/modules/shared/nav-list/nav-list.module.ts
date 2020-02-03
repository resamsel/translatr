import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavListComponent } from './nav-list.component';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { ListHeaderModule } from '../list-header/list-header.module';
import { MatCardModule } from '@angular/material';
import { RouterModule } from '@angular/router';

@NgModule({
  declarations: [NavListComponent],
  imports: [
    CommonModule,
    ListHeaderModule,

    MatListModule,
    MatIconModule,
    MatCardModule,
    RouterModule
  ],
  exports: [NavListComponent]
})
export class NavListModule {}
