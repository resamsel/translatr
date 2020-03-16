import { Component, Inject, Injector, OnInit } from '@angular/core';
import { PagedList, Project, User } from '@dev/translatr-model';
import { UserFacade } from '../+state/user.facade';
import { filter, map, take, takeUntil } from 'rxjs/operators';
import { openProjectEditDialog } from '../../../shared/project-edit-dialog/project-edit-dialog.component';
import { MatDialog } from '@angular/material';
import { ActivatedRoute, CanActivate, Router } from '@angular/router';
import { canActivate$, NameIconRoute, slicePagedList } from '@translatr/utils';
import { USER_ROUTES } from '../user-page.token';
import { Observable, of } from 'rxjs';

@Component({
  selector: 'app-user-info',
  templateUrl: './user-info.component.html',
  styleUrls: ['./user-info.component.scss']
})
export class UserInfoComponent implements OnInit {
  readonly user$ = this.facade.user$;
  readonly projects$ = this.facade.projects$
    .pipe(
      map((pagedList: PagedList<Project>) => !!pagedList
        ? {
          ...pagedList,
          list: pagedList.list.slice(0, 3)
        }
        : pagedList)
    );
  readonly canCreateProject$ = this.facade.canCreateProject$;
  readonly activities$ = this.facade.activities$
    .pipe(map(pagedList => slicePagedList(pagedList, 8)));

  readonly activityRoute: NameIconRoute | undefined = this.routes[0].children
    .find(route => route.path === 'activity');
  readonly canReadActivity$ = this.facade.canReadActivity$;
  readonly activityLink$ = this.canActivate$(this.activityRoute)
    .pipe(map(active => active ? 'activity' : undefined));

  constructor(
    private readonly facade: UserFacade,
    private readonly dialog: MatDialog,
    private readonly router: Router,
    private readonly injector: Injector,
    private readonly route: ActivatedRoute,
    @Inject(USER_ROUTES) private routes: { children: NameIconRoute[] }[]
  ) {
  }

  ngOnInit() {
    this.user$
      .pipe(
        filter(user => !!user),
        takeUntil(this.facade.destroy$)
      )
      .subscribe((user: User) => {
        this.facade.loadProjects({
          owner: user.username,
          order: 'whenUpdated desc',
          fetch: 'count'
        });
        this.facade.loadActivities({
          userId: user.id,
          types: 'Create,Update,Delete'
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

  canActivate$(route: NameIconRoute): Observable<boolean> {
    if (route === undefined) {
      return of(false);
    }

    return canActivate$(
      route,
      this.route,
      (guard: any) => this.injector.get<CanActivate>(guard)
    );
  }
}
