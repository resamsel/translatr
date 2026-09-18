import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { RouterModule } from '@angular/router';
import { FeatureFlagDirective, FeatureFlagClassDirective } from '@dev/translatr-components';
import { SidenavModule } from '../nav/sidenav/sidenav.module';
import { AdminPageComponent } from './admin-page.component';
import { MatIconButton } from '@angular/material/button';

@NgModule({
  declarations: [AdminPageComponent],
  imports: [
    CommonModule,
    RouterModule,
    SidenavModule,
    FeatureFlagDirective, FeatureFlagClassDirective,

    MatSidenavModule,
    MatToolbarModule,
    MatIconModule,
    MatListModule,
    MatIconButton,
    MatMenuModule,
  ],
  exports: [AdminPageComponent],
})
export class AdminPageModule {}
