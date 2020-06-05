import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { ErrorPageHeaderComponent } from './error-page-header.component';
import { ErrorPageMessageComponent } from './error-page-message.component';
import { ErrorPageComponent } from './error-page.component';

@NgModule({
  declarations: [ErrorPageComponent, ErrorPageHeaderComponent, ErrorPageMessageComponent],
  exports: [ErrorPageComponent, ErrorPageHeaderComponent, ErrorPageMessageComponent],
  imports: [CommonModule, MatIconModule]
})
export class ErrorPageModule {}
