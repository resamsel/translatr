import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { User, UserRole } from '@dev/translatr-model';
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
  @Input() showDashboardLink = false;

  readonly endpointUrl = environment.endpointUrl;
  readonly adminUrl = environment.adminUrl;

  isAdmin(me: User | undefined): boolean {
    return !!me && me.role === UserRole.Admin;
  }
}
