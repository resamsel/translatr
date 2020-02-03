import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ProjectCriteria, User } from '@dev/translatr-model';
import { openProjectEditDialog } from '../../../shared/project-edit-dialog/project-edit-dialog.component';
import { filter, take, takeUntil, withLatestFrom } from 'rxjs/operators';
import { MatDialog } from '@angular/material/dialog';
import { UserFacade } from '../+state/user.facade';
import { FilterCriteria } from '../../../shared/list-header/list-header.component';
import { navigate } from '@translatr/utils';

@Component({
  selector: 'app-user-projects',
  templateUrl: './user-projects.component.html',
  styleUrls: ['./user-projects.component.scss']
})
export class UserProjectsComponent implements OnInit {
  projects$ = this.facade.projects$;
  canCreateProject$ = this.facade.canCreateProject$;
  criteria$ = this.facade.projectsCriteria$;

  constructor(
    private readonly facade: UserFacade,
    private readonly router: Router,
    private readonly dialog: MatDialog
  ) {
  }

  ngOnInit() {
    this.criteria$
      .pipe(
        withLatestFrom(this.facade.user$.pipe(filter(x => !!x))),
        takeUntil(this.facade.destroy$)
      )
      .subscribe(([criteria, user]: [ProjectCriteria, User]) => {
        this.facade.loadProjects({
          owner: user.username,
          ...criteria
        });
      });
  }

  onFilter(criteria: FilterCriteria): void {
    navigate(this.router, criteria);
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
