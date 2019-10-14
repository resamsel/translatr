import { Component, OnDestroy, OnInit } from '@angular/core';
import { PagedList, Project, User } from '@dev/translatr-model';
import { UserFacade } from '../+state/user.facade';
import { filter, map, take, takeUntil } from 'rxjs/operators';
import { Subject } from 'rxjs';
import { openProjectEditDialog } from '../../../shared/project-edit-dialog/project-edit-dialog.component';
import { MatDialog } from '@angular/material';
import { Router } from '@angular/router';

@Component({
  selector: 'app-user-info',
  templateUrl: './user-info.component.html',
  styleUrls: ['./user-info.component.scss']
})
export class UserInfoComponent implements OnInit, OnDestroy {
  private readonly destroy$ = new Subject<void>();

  user$ = this.facade.user$
    .pipe(takeUntil(this.destroy$.asObservable()));
  projects$ = this.facade.projects$
    .pipe(
      map((pagedList: PagedList<Project>) => !!pagedList
        ? {
          ...pagedList,
          list: pagedList.list.slice(0, 3)
        }
        : pagedList),
      takeUntil(this.destroy$.asObservable())
    );
  canCreateProject$ = this.facade.canCreateProject$;
  activities$ = this.facade.activities$
    .pipe(takeUntil(this.destroy$.asObservable()));

  constructor(
    private readonly facade: UserFacade,
    private readonly dialog: MatDialog,
    private readonly router: Router
  ) {
  }

  ngOnInit() {
    this.user$
      .pipe(filter(user => !!user))
      .subscribe((user: User) => {
        this.facade.loadProjects({
          owner: user.username,
          order: 'whenUpdated desc'
        });
        this.facade.loadActivities({
          userId: user.id
        });
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
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
}
