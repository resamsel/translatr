import { Component } from '@angular/core';
import { ProjectFacade } from '../+state/project.facade';
import { map, pluck } from 'rxjs/operators';
import { combineLatest } from 'rxjs';
import { navigate } from '@translatr/utils';
import { Router } from '@angular/router';

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
  canCreate$ = this.facade.canCreateMember$;

  constructor(
    private readonly facade: ProjectFacade,
    private readonly router: Router
  ) {
  }

  onFilter(search: string): void {
    navigate(this.router, { search });
  }
}
