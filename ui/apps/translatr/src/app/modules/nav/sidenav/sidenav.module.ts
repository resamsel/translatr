import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterModule } from '@angular/router';
import {
  AuthBarItemComponent,
  AuthBarLanguageSwitcherComponent,
  FeatureFlagDirective,
  FeatureFlagClassDirective,
  FooterComponent,
  LanguageSwicher,
  NavbarComponent,
  SearchBarComponent
} from '@dev/translatr-components';
import { TranslocoModule } from '@jsverse/transloco';
import { AppFacade } from '../../../+state/app.facade';
import { SidenavComponent } from './sidenav.component';

@NgModule({
  declarations: [SidenavComponent],
  imports: [
    CommonModule,
    RouterModule,
    FooterComponent,
    NavbarComponent,
    AuthBarItemComponent,
    AuthBarLanguageSwitcherComponent,
    SearchBarComponent,

    MatSidenavModule,
    MatToolbarModule,
    MatIconModule,
    MatButtonModule,
    MatListModule,
    MatMenuModule,
    MatDividerModule,
    MatTooltipModule,
    FeatureFlagDirective, FeatureFlagClassDirective,
    TranslocoModule
  ],
  providers: [{ provide: LanguageSwicher, useClass: AppFacade }],
  exports: [SidenavComponent]
})
export class SidenavModule {}
