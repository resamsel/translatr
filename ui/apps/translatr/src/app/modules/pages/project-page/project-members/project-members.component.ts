import { Component } from '@angular/core';
import { ProjectFacade } from '../+state/project.facade';
import { filter, map, pluck, take, takeUntil, withLatestFrom } from 'rxjs/operators';
import { combineLatest, Observable } from 'rxjs';
import { navigate } from '@translatr/utils';
import { Router } from '@angular/router';
import { Member, MemberCriteria, Project, User } from '@dev/translatr-model';
import { MatSnackBar } from '@angular/material';
import { AppFacade } from '../../../../+state/app.facade';
import { FilterCriteria } from '../../../shared/list-header/list-header.component';

@Component({
  selector: 'app-project-members',
  templateUrl: './project-members.component.html',
  styleUrls: ['./project-members.component.scss']
})
export class ProjectMembersComponent {
  project$ = this.facade.project$;
  criteria$ = this.facade.membersCriteria$;
  members$ = this.facade.members$;

  canModify$ = this.facade.canModifyMember$;
  canModifyOwner$: Observable<boolean> = combineLatest([
    this.facade.members$.pipe(filter(x => !!x), pluck('list')),
    this.appFacade.me$.pipe(filter(x => !!x))
  ])
    .pipe(
      map(([members, me]: [Array<Member>, User]) =>
        members.find(m => m.userId === me.id)),
      map((member: Member | undefined) =>
        member !== undefined ? member.role === 'Owner' : false)
    );
  canTransferOwnership$: Observable<boolean> = combineLatest([
    this.project$.pipe(filter(x => !!x), pluck<Project, string>('ownerId')),
    this.appFacade.me$.pipe(filter(x => !!x))
  ])
    .pipe(
      map(([ownerId, me]: [string, User]) => ownerId === me.id)
    );

  constructor(
    private readonly facade: ProjectFacade,
    private readonly appFacade: AppFacade,
    private readonly router: Router,
    private readonly snackBar: MatSnackBar
  ) {
    this.criteria$
      .pipe(
        withLatestFrom(this.project$.pipe(filter(x => !!x), take(1))),
        takeUntil(this.facade.unload$)
      )
      .subscribe(([criteria, project]: [MemberCriteria, Project]) =>
        this.facade.loadMembers(project.id, criteria)
      );
  }

  onFilter(criteria: FilterCriteria): void {
    navigate(this.router, criteria);
  }

  onDelete(member: Member) {
    this.facade.deleteMember(member.id);
    this.facade.memberModified$
      .pipe(
        map(([a]) => a),
        filter(x => !!x),
        take(1),
        withLatestFrom(this.appFacade.me$)
      )
      .subscribe(([m, me]: [Member, User]) => {
        if (m.userId === me.id) {
          // I have removed myself from the project, go to dashboard
          this.router.navigate(['/dashboard']);
        }

        this.snackBar.open(
          `${m.userName} removed from project`,
          'Dismiss',
          { duration: 5000 }
        );
      });
  }
}
