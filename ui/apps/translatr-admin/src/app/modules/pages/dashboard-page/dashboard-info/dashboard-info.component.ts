import { Component } from '@angular/core';
import { AppFacade } from '../../../../+state/app.facade';

@Component({
  selector: 'dev-dashboard-info',
  templateUrl: './dashboard-info.component.html',
  styleUrls: ['./dashboard-info.component.css']
})
export class DashboardInfoComponent {
  users$ = this.facade.users$;
  projects$ = this.facade.projects$;
  accessTokens$ = this.facade.accessTokens$;
  activities$ = this.facade.activities$;

  constructor(private readonly facade: AppFacade) {
    facade.loadUsers({ limit: 1, fetch: 'count' });
    facade.loadProjects({ limit: 1, fetch: 'count' });
    facade.loadAccessTokens({ limit: 1, fetch: 'count' });
    facade.loadActivities({ limit: 1, fetch: 'count' });
  }
}
