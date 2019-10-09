import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotFoundPageComponent } from './not-found-page.component';
import { NotFoundPageRoutingModule } from './not-found-page-routing.module';
import { SidenavModule } from '../../nav/sidenav/sidenav.module';
import { MatButtonModule, MatCardModule, MatDividerModule } from '@angular/material';

@NgModule({
  declarations: [NotFoundPageComponent],
  imports: [
    CommonModule,
    NotFoundPageRoutingModule,
    SidenavModule,
    MatCardModule,
    MatDividerModule,
    MatButtonModule
  ]
})
export class NotFoundPageModule {
}
