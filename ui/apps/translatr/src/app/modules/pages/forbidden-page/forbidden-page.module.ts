import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ForbiddenPageComponent } from './forbidden-page.component';
import { MatButtonModule } from '@angular/material/button';
import { RouterModule } from '@angular/router';
import { ErrorPageModule } from '@dev/translatr-components';
import { ForbiddenPageRoutingModule } from './forbidden-page-routing.module';

@NgModule({
  declarations: [ForbiddenPageComponent],
  imports: [
    ForbiddenPageRoutingModule,
    CommonModule,
    MatButtonModule,
    RouterModule,
    ErrorPageModule
  ]
})
export class ForbiddenPageModule {
}
