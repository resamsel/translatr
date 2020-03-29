import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { SidenavComponent } from './sidenav.component';
import { RouterModule } from '@angular/router';
import { FeatureFlagModule, FooterModule, NavbarModule } from '@dev/translatr-components';
import { MatTooltipModule } from '@angular/material';
import { AppFacade } from '../../../+state/app.facade';

@NgModule({
  declarations: [SidenavComponent],
  imports: [
    CommonModule,
    RouterModule,
    FooterModule,
    NavbarModule.forRoot(AppFacade),

    MatSidenavModule,
    MatToolbarModule,
    MatIconModule,
    MatButtonModule,
    MatListModule,
    MatMenuModule,
    MatDividerModule,
    MatTooltipModule,
    FeatureFlagModule
  ],
  exports: [SidenavComponent]
})
export class SidenavModule {}
