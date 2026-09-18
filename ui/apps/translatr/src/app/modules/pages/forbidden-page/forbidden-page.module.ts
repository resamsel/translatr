import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { RouterModule } from '@angular/router';
import { ErrorPageComponent, ErrorPageHeaderComponent, ErrorPageMessageComponent } from '@dev/translatr-components';
import { ForbiddenPageRoutingModule } from './forbidden-page-routing.module';
import { ForbiddenPageComponent } from './forbidden-page.component';

@NgModule({
  declarations: [ForbiddenPageComponent],
  imports: [
    ForbiddenPageRoutingModule,
    CommonModule,
    MatButtonModule,
    RouterModule,
    ErrorPageComponent, ErrorPageHeaderComponent, ErrorPageMessageComponent
  ]
})
export class ForbiddenPageModule {}
