import { Component, OnInit } from '@angular/core';
import { AppFacade } from '../../../+state/app.facade';
import { ProjectsFacade } from '../projects-page/+state/projects.facade';
import { filter, take } from 'rxjs/operators';
import { Project, User } from '@dev/translatr-model';
import { DashboardFacade } from './+state/dashboard.facade';
import { openProjectCreationDialog } from '../../shared/project-creation-dialog/project-creation-dialog.component';
import { MatDialog } from '@angular/material';
import { Router } from '@angular/router';

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
    private readonly projectsFacade: ProjectsFacade,
    private readonly dialog: MatDialog,
    private readonly router: Router
  ) {
  }

  ngOnInit() {
    this.me$
      .pipe(
        filter(me => me !== undefined),
        take(1)
      )
      .subscribe((user: User) => {
        this.loadProjects(user);
        this.dashboardFacade.loadActivities({
          projectOwnerId: user.id,
          limit: 4
        });
      });
  }

  openProjectCreationDialog(): void {
    openProjectCreationDialog(this.dialog)
      .afterClosed()
      .pipe(
        take(1),
        filter(project => !!project)
      )
      .subscribe((project => this.router
        .navigate([project.ownerUsername, project.name])));
  }

  private loadProjects(user: User): void {
    this.projectsFacade.loadProjects({
      owner: user.username,
      limit: '4',
      order: 'whenUpdated desc'
    });
  }
}
