import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { Feature, User, UserRole } from '@dev/translatr-model';
import { environment } from '../../../../environments/environment';
import { Link } from '@dev/translatr-components';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-sidenav',
  templateUrl: './sidenav.component.html',
  styleUrls: ['./sidenav.component.scss']
})
export class SidenavComponent {
  @Input() page: string;
  @Input() backLink: Link;
  @Input() me: User | undefined;
  @Input() elevated = true;
  @Input() overlay = false;
  @Input() showDashboardLink = false;

  @Output() languageSwitch = new EventEmitter<string>();

  readonly endpointUrl = environment.endpointUrl;
  readonly adminUrl = environment.adminUrl;
  readonly Feature = Feature;

  isAdmin(me: User | undefined): boolean {
    return !!me && me.role === UserRole.Admin;
  }
}
