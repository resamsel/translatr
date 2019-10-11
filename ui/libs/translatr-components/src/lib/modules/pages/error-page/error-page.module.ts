import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ErrorPageComponent } from './error-page.component';
import { MatIconModule } from '@angular/material';
import { ErrorPageHeaderComponent } from './error-page-header.component';
import { ErrorPageMessageComponent } from './error-page-message.component';

@NgModule({
  declarations: [ErrorPageComponent, ErrorPageHeaderComponent, ErrorPageMessageComponent],
  exports: [
    ErrorPageComponent,
    ErrorPageHeaderComponent,
    ErrorPageMessageComponent
  ],
  imports: [
    CommonModule,
    MatIconModule
  ]
})
export class ErrorPageModule {
}
