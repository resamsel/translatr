import { Component } from '@angular/core';
import { ProjectFacade } from '../+state/project.facade';
import { MatDialog } from '@angular/material/dialog';
import { map, pluck } from 'rxjs/operators';
import { BehaviorSubject, combineLatest } from 'rxjs';

@Component({
  selector: 'app-project-members',
  templateUrl: './project-members.component.html',
  styleUrls: ['./project-members.component.scss']
})
export class ProjectMembersComponent {
  project$ = this.facade.project$;
  memberFilter$ = new BehaviorSubject<string>('');
  members$ = combineLatest(
    this.project$.pipe(pluck('members')),
    this.memberFilter$.pipe(map(s => s.toLocaleLowerCase()))
  ).pipe(
    map(([members, search]) => members
      .slice()
      .filter(member => member.userName
          .toLocaleLowerCase()
          .includes(search)
        || member.userUsername
          .toLocaleLowerCase()
          .includes(search)))
  );

  constructor(
    private readonly facade: ProjectFacade,
    private readonly dialog: MatDialog
  ) {
  }

  onFilter(search: string): void {
    this.memberFilter$.next(search);
  }
}
