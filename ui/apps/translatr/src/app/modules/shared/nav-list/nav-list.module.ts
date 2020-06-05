import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { RouterModule } from '@angular/router';
import { TranslocoModule } from '@ngneat/transloco';
import { ListHeaderModule } from '../list-header/list-header.module';
import { NavListComponent } from './nav-list.component';

@NgModule({
  declarations: [NavListComponent],
  imports: [
    CommonModule,
    ListHeaderModule,

    MatListModule,
    MatIconModule,
    MatCardModule,
    RouterModule,
    MatButtonModule,
    TranslocoModule
  ],
  exports: [NavListComponent]
})
export class NavListModule {}
