import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { Feature, User } from '@dev/translatr-model';
import { filter, take } from 'rxjs/operators';
import { AppFacade } from '../../../+state/app.facade';
import { openProjectEditDialog } from '../../shared/project-edit-dialog/project-edit-dialog.component';
import { ProjectsFacade } from '../projects-page/+state/projects.facade';
import { DashboardFacade } from './+state/dashboard.facade';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
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

  readonly Feature = Feature;

  constructor(
    private readonly appFacade: AppFacade,
    private readonly dashboardFacade: DashboardFacade,
    private readonly projectsFacade: ProjectsFacade,
    private readonly dialog: MatDialog,
    private readonly router: Router
  ) {}

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
          limit: 10,
          fetch: 'count',
          types: 'Create,Update,Delete'
        });
      });
  }

  openProjectCreationDialog(): void {
    openProjectEditDialog(this.dialog, {})
      .afterClosed()
      .pipe(
        filter(project => !!project),
        take(1)
      )
      .subscribe(project => this.router.navigate([project.ownerUsername, project.name]));
  }

  private loadMyProjects(user: User): void {
    this.projectsFacade.loadMyProjects({
      owner: user.username,
      limit: 4,
      order: 'whenUpdated desc',
      fetch: 'count'
    });
  }
}
