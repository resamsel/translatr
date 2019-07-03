import { Component, OnInit } from '@angular/core';
import { AppFacade } from '../../../+state/app.facade';
import { ProjectsFacade } from '../projects-page/+state/projects.facade';
import { take } from 'rxjs/operators';
import { User } from '@dev/translatr-model';
import { DashboardFacade } from './+state/dashboard.facade';

@Component({
  selector: 'app-dashboard-page',
  templateUrl: './dashboard-page.component.html',
  styleUrls: ['./dashboard-page.component.scss']
})
export class DashboardPageComponent implements OnInit {
  readonly me$ = this.appFacade.me$;
  readonly projects$ = this.projectsFacade.allProjects$;
  readonly activities$ = this.dashboardFacade.activities$;

  constructor(
    private readonly appFacade: AppFacade,
    private readonly dashboardFacade: DashboardFacade,
    private readonly projectsFacade: ProjectsFacade
  ) {
  }

  ngOnInit() {
    this.me$
      .pipe(take(1))
      .subscribe((user: User) => {
        this.projectsFacade.loadProjects({ owner: user.id });
        this.dashboardFacade.loadProjectActivities(user.id);
      });
  }
}
