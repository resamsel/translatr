import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDividerModule } from '@angular/material/divider';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatTableModule } from '@angular/material/table';
import { TranslocoModule } from '@ngneat/transloco';
import { ButtonModule } from '../../button';
import { FilterFieldModule } from '../../filter-field';
import { EntityTableComponent } from './entity-table.component';
import { SelectionActionsComponent } from './selection-actions.component';

@NgModule({
  declarations: [EntityTableComponent, SelectionActionsComponent],
  exports: [EntityTableComponent, SelectionActionsComponent],
  imports: [
    CommonModule,
    ButtonModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatCheckboxModule,
    MatInputModule,
    MatFormFieldModule,
    MatPaginatorModule,
    MatDividerModule,
    FilterFieldModule,
    TranslocoModule
  ]
})
export class EntityTableModule {}
