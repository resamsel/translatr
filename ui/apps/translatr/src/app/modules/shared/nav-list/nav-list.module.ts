import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavListComponent } from './nav-list.component';
import { MatIconModule, MatListModule } from '@angular/material';

@NgModule({
  declarations: [NavListComponent],
  imports: [CommonModule, MatListModule, MatIconModule],
  exports: [NavListComponent]
})
export class NavListModule {}
