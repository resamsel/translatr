import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MainPageComponent } from './main-page.component';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { LayoutModule } from '@angular/cdk/layout';
import { MainPageRoutingModule } from './main-page-routing.module';
import { ActivityGraphModule, FeatureFlagModule, FooterModule, NavbarModule } from '@dev/translatr-components';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TranslocoModule } from '@ngneat/transloco';
import { AppFacade } from '../../../+state/app.facade';

@NgModule({
  declarations: [MainPageComponent],
  imports: [
    MainPageRoutingModule,
    CommonModule,
    NavbarModule.forRoot(AppFacade),
    MatGridListModule,
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    MatMenuModule,
    LayoutModule,
    MatDividerModule,
    MatTooltipModule,
    FooterModule,
    ActivityGraphModule,
    TranslocoModule,
    FeatureFlagModule
  ]
})
export class MainPageModule {}
