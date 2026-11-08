import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Params, Router } from '@angular/router';
import { FeatureFlagClassDirective } from '@dev/translatr-components';
import { Feature, ProjectCriteria, User } from '@dev/translatr-model';
import { TranslocoModule } from '@jsverse/transloco';
import { navigate } from '@translatr/utils';
import { combineLatest } from 'rxjs';
import { distinctUntilChanged, filter, map, take, takeUntil } from 'rxjs/operators';
import { AppFacade } from '../../../+state/app.facade';
import { SidenavComponent } from '../../nav/sidenav/sidenav.component';
import { FilterCriteria } from '../../shared/list-header/list-header.component';
import { openProjectEditDialog } from '../../shared/project-edit-dialog/project-edit-dialog.component';
import { ProjectListComponent } from '../../shared/project-list/project-list.component';
import { ProjectsFacade } from './+state/projects.facade';

@Component({
  standalone: true,
  selector: 'app-projects-page',
  templateUrl: './projects-page.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./projects-page.component.scss'],
  imports: [
    CommonModule,
    TranslocoModule,
    SidenavComponent,
    ProjectListComponent,
    MatIconModule,
    MatButtonModule,
    MatTooltipModule,
    FeatureFlagClassDirective
  ]
})
export class ProjectsPageComponent implements OnInit, OnDestroy {
  me$ = this.appFacade.me$;
  projects$ = this.facade.projects$;

  criteria$ = this.appFacade.queryParams$.pipe(
    map((params: Params) =>
      ['search', 'limit', 'offset']
        .filter(f => params[f] !== undefined && params[f] !== '')
        .reduce((acc, curr) => ({ ...acc, [curr]: params[curr] }), {})
    ),
    distinctUntilChanged(
      (a: ProjectCriteria, b: ProjectCriteria) =>
        a.search === b.search && a.limit === b.limit && a.offset === b.offset
    )
  );
  private loadProjects$ = combineLatest([this.me$.pipe(filter(user => !!user)), this.criteria$]);

  readonly Feature = Feature;

  constructor(
    private readonly facade: ProjectsFacade,
    private readonly appFacade: AppFacade,
    private readonly dialog: MatDialog,
    private readonly router: Router
  ) {}

  ngOnInit() {
    this.loadProjects$
      .pipe(takeUntil(this.facade.unload$))
      .subscribe(([user, criteria]: [User, ProjectCriteria]) =>
        this.facade.loadProjects({
          memberId: user.id,
          order: 'whenUpdated desc',
          limit: 20,
          fetch: 'count,progress',
          ...criteria
        })
      );
  }

  ngOnDestroy(): void {
    this.facade.unloadProjects();
  }

  openProjectCreationDialog() {
    openProjectEditDialog(this.dialog, {})
      .afterClosed()
      .pipe(
        take(1),
        filter(project => !!project)
      )
      .subscribe(project => this.router.navigate([project.ownerUsername, project.name]));
  }

  onFilter(criteria: FilterCriteria): void {
    navigate(this.router, criteria);
  }
}
