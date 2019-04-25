import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {EntityTableComponent} from './entity-table.component';
import {MatButtonModule, MatCheckboxModule, MatFormFieldModule, MatIconModule, MatInputModule, MatTableModule} from '@angular/material';
import {ButtonModule} from '../../button';
import {SelectionActionsComponent} from './selection-actions.component';

@NgModule({
  declarations: [
    EntityTableComponent,
    SelectionActionsComponent
  ],
  exports: [
    EntityTableComponent,
    SelectionActionsComponent
  ],
  imports: [
    CommonModule,
    ButtonModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatCheckboxModule,
    MatInputModule,
    MatFormFieldModule
  ]
})
export class EntityTableModule {
}
