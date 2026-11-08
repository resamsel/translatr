import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterModule } from '@angular/router';
import {
  AuthBarItemComponent,
  AuthBarLanguageSwitcherComponent,
  FeatureFlagDirective,
  FooterComponent,
  LanguageSwicher,
  Link,
  NavbarComponent
} from '@dev/translatr-components';
import { Feature, User, UserRole } from '@dev/translatr-model';
import { TranslocoModule } from '@jsverse/transloco';
import { AppFacade } from '../../../+state/app.facade';
import { environment } from '../../../../environments/environment';

@Component({
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-sidenav',
  templateUrl: './sidenav.component.html',
  styleUrls: ['./sidenav.component.scss'],
  imports: [
    CommonModule,
    RouterModule,
    TranslocoModule,
    NavbarComponent,
    AuthBarItemComponent,
    AuthBarLanguageSwitcherComponent,
    FooterComponent,
    FeatureFlagDirective,
    MatIconModule,
    MatButtonModule,
    MatMenuModule,
    MatDividerModule,
    MatTooltipModule
  ],
  providers: [{ provide: LanguageSwicher, useClass: AppFacade }]
})
export class SidenavComponent {
  @Input() page: string;
  @Input() backLink: Link;
  @Input() me: User | undefined;
  @Input() elevated = true;
  @Input() overlay = false;
  @Input() showDashboardLink = false;

  readonly endpointUrl = environment.endpointUrl;
  readonly adminUrl = environment.adminUrl;
  readonly Feature = Feature;

  isAdmin(me: User | undefined): boolean {
    return !!me && me.role === UserRole.Admin;
  }
}
