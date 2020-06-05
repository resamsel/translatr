import { ChangeDetectionStrategy, Component } from '@angular/core';
import { Feature, User } from '@dev/translatr-model';
import { AppFacade } from '../../../../+state/app.facade';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'dev-dashboard-info',
  templateUrl: './dashboard-info.component.html',
  styleUrls: ['./dashboard-info.component.scss']
})
export class DashboardInfoComponent {
  users$ = this.facade.users$;
  projects$ = this.facade.projects$;
  accessTokens$ = this.facade.accessTokens$;
  activities$ = this.facade.activities$;

  readonly Feature = Feature;

  constructor(private readonly facade: AppFacade) {
    facade.loadUsers({ limit: 1, fetch: 'count', order: 'whenCreated desc' });
    facade.loadProjects({ limit: 1, fetch: 'count', order: 'whenCreated desc' });
    facade.loadAccessTokens({ limit: 1, fetch: 'count', order: 'whenCreated desc' });
    facade.loadActivities({ limit: 1, fetch: 'count', order: 'whenCreated desc' });
  }

  userLink(user: User | undefined | null): string[] | undefined {
    if (user === undefined || user === null) {
      return undefined;
    }

    return ['users', user.id];
  }
}
