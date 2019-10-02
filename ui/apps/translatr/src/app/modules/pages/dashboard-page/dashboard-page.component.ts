import { Component, OnInit } from '@angular/core';
import { AppFacade } from '../../../+state/app.facade';
import { ProjectsFacade } from '../projects-page/+state/projects.facade';
import { filter, take } from 'rxjs/operators';
import { User } from '@dev/translatr-model';
import { DashboardFacade } from './+state/dashboard.facade';
import { openProjectEditDialog } from '../../shared/project-edit-dialog/project-edit-dialog.component';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';

@Component({
  selector: 'app-dashboard-page',
  templateUrl: './dashboard-page.component.html',
  styleUrls: ['./dashboard-page.component.scss']
})
export class DashboardPageComponent implements OnInit {
  readonly me$ = this.appFacade.me$;
  readonly myProjects$ = this.projectsFacade.myProjects$;
  readonly activities$ = this.dashboardFacade.activities$;
  readonly projects$ = this.projectsFacade.projects$;
  readonly users$ = this.appFacade.users$;

  constructor(
    private readonly appFacade: AppFacade,
    private readonly dashboardFacade: DashboardFacade,
    private readonly projectsFacade: ProjectsFacade,
    private readonly dialog: MatDialog,
    private readonly router: Router
  ) {
  }

  ngOnInit() {
    this.appFacade.loadUsers({ limit: 1, fetch: 'count' });
    this.me$
      .pipe(
        filter(me => me !== undefined),
        take(1)
      )
      .subscribe((user: User) => {
        this.loadMyProjects(user);
        this.projectsFacade.loadProjects({
          memberId: user.id,
          limit: 4,
          fetch: 'count'
        });
        this.dashboardFacade.loadActivities({
          userId: user.id,
          limit: 4,
          fetch: 'count'
        });
      });
  }

  openProjectCreationDialog(): void {
    openProjectEditDialog(this.dialog, {})
      .afterClosed()
      .pipe(
        take(1),
        filter(project => !!project)
      )
      .subscribe((project => this.router
        .navigate([project.ownerUsername, project.name])));
  }

  private loadMyProjects(user: User): void {
    this.projectsFacade.loadMyProjects({
      owner: user.username,
      limit: 4,
      order: 'whenUpdated desc'
    });
  }
}
