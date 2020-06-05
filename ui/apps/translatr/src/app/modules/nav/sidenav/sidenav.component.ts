import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { Link } from '@dev/translatr-components';
import { Feature, User, UserRole } from '@dev/translatr-model';
import { environment } from '../../../../environments/environment';

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

  readonly endpointUrl = environment.endpointUrl;
  readonly adminUrl = environment.adminUrl;
  readonly Feature = Feature;

  isAdmin(me: User | undefined): boolean {
    return !!me && me.role === UserRole.Admin;
  }
}
