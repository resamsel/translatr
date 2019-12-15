import { Component } from '@angular/core';
import { ProjectFacade } from '../+state/project.facade';
import { filter, map, pluck, skip, take, withLatestFrom } from 'rxjs/operators';
import { combineLatest, Observable } from 'rxjs';
import { navigate } from '@translatr/utils';
import { Router } from '@angular/router';
import { Member, User } from '@dev/translatr-model';
import { MatSnackBar } from '@angular/material';
import { AppFacade } from '../../../../+state/app.facade';

@Component({
  selector: 'app-project-members',
  templateUrl: './project-members.component.html',
  styleUrls: ['./project-members.component.scss']
})
export class ProjectMembersComponent {
  project$ = this.facade.project$;
  criteria$ = this.facade.criteria$;
  memberFilter$ = this.criteria$.pipe(pluck('search'));
  members$ = combineLatest([
    this.project$.pipe(pluck('members')),
    this.memberFilter$.pipe(map(s => !!s ? s.toLocaleLowerCase() : ''))
  ]).pipe(
    map(([members, search]) => members
      .slice()
      .filter(member => member.userName
          .toLocaleLowerCase()
          .includes(search)
        || member.userUsername
          .toLocaleLowerCase()
          .includes(search)))
  );
  canModify$ = this.facade.canModifyMember$;
  canModifyOwner$: Observable<boolean> = combineLatest([
    this.project$.pipe(pluck('members')),
    this.appFacade.me$.pipe(filter(x => !!x))
  ]).pipe(
    map(([members, me]: [Array<Member>, User]) =>
      members.find(m => m.userId === me.id)),
    map((member: Member | undefined) =>
      member !== undefined ? member.role === 'Owner' : false)
  );
  canTransferOwnership$: Observable<boolean> = combineLatest([
    this.project$.pipe(pluck('ownerId')),
    this.appFacade.me$.pipe(filter(x => !!x))
  ]).pipe(
    map(([ownerId, me]: [string, User]) => ownerId === me.id)
  );

  constructor(
    private readonly facade: ProjectFacade,
    private readonly appFacade: AppFacade,
    private readonly router: Router,
    private readonly snackBar: MatSnackBar
  ) {
  }

  onFilter(search: string): void {
    navigate(this.router, { search });
  }

  onDelete(member: Member) {
    this.facade.deleteMember(member.id);
    this.facade.memberModified$
      .pipe(skip(1), take(1), withLatestFrom(this.appFacade.me$))
      .subscribe(([m, me]: [Member, User]) => {
        this.snackBar.open(
          `${m.userName} removed from project`,
          'Dismiss',
          { duration: 5000 }
        );
        if (m.userId === me.id) {
          // I have removed myself from the project, go to dashboard
          this.router.navigate(['/dashboard']);
        }
      });
  }
}
