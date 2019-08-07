import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavListComponent } from './nav-list.component';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';

@NgModule({
  declarations: [NavListComponent],
  imports: [CommonModule, MatListModule, MatIconModule],
  exports: [NavListComponent]
})
export class NavListModule {}
