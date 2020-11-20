import { LayoutModule } from '@angular/cdk/layout';
import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltipModule } from '@angular/material/tooltip';
import {
  ActivityGraphModule,
  FeatureFlagModule,
  FooterModule,
  MetricModule,
  NavbarModule,
  ShortNumberModule
} from '@dev/translatr-components';
import { SvgIconsModule } from '@ngneat/svg-icon';
import { TranslocoModule } from '@ngneat/transloco';
import { AppFacade } from '../../../+state/app.facade';
import {
  appChat,
  appDocument,
  appEducation,
  appPulse,
  appThumbsUp,
  appUser,
  appWrite
} from '../../../../assets';
import { MainPageRoutingModule } from './main-page-routing.module';
import { MainPageComponent } from './main-page.component';

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
    FeatureFlagModule,
    MetricModule,
    ShortNumberModule,
    SvgIconsModule.forChild([
      appPulse,
      appUser,
      appThumbsUp,
      appEducation,
      appChat,
      appWrite,
      appDocument
    ])
  ]
})
export class MainPageModule {}
