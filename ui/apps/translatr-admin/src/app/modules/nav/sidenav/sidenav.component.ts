import { CommonModule } from '@angular/common';
import { Component, Input, ChangeDetectionStrategy } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatDrawer } from '@angular/material/sidenav';
import { MatTooltipModule } from '@angular/material/tooltip';
import {
  AuthBarItemComponent,
  AuthBarLanguageSwitcherComponent,
  FeatureFlagDirective,
  FooterComponent,
  LanguageSwicher,
  Link,
  NavbarComponent
} from '@dev/translatr-components';
import { Feature, User } from '@dev/translatr-model';
import { TranslocoModule } from '@jsverse/transloco';
import { AppFacade } from '../../../+state/app.facade';
import { environment } from '../../../../environments/environment';

@Component({
  standalone: true,
  selector: 'app-sidenav',
  templateUrl: './sidenav.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./sidenav.component.scss'],
  imports: [
    CommonModule,
    TranslocoModule,
    NavbarComponent,
    AuthBarItemComponent,
    AuthBarLanguageSwitcherComponent,
    FooterComponent,
    FeatureFlagDirective,
    MatIconModule,
    MatMenuModule,
    MatTooltipModule
  ],
  providers: [{ provide: LanguageSwicher, useClass: AppFacade }]
})
export class SidenavComponent {
  @Input() page: string;
  @Input() backLink: Link;
  @Input() me: User | undefined;
  @Input() sidenav: MatDrawer;
  @Input() showFooter = true;
  @Input() overlay = false;
  @Input() showLogo = true;
  @Input() headerColor: string;

  readonly endpointUrl = environment.endpointUrl;
  readonly uiUrl = environment.uiUrl;

  readonly Feature = Feature;
}
