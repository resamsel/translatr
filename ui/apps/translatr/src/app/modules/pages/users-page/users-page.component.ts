import { Component, OnDestroy, OnInit } from '@angular/core';
import { Params, Router } from '@angular/router';
import { Feature } from '@dev/translatr-model';
import { navigate } from '@translatr/utils';
import { distinctUntilChanged, map, takeUntil } from 'rxjs/operators';
import { AppFacade } from '../../../+state/app.facade';
import { FilterCriteria } from '../../shared/list-header/list-header.component';
import { UserCriteria } from './+state/users.actions';
import { UsersFacade } from './+state/users.facade';

@Component({
  selector: 'app-projects-page',
  templateUrl: './users-page.component.html',
  styleUrls: ['./users-page.component.scss']
})
export class UsersPageComponent implements OnInit, OnDestroy {
  me$ = this.appFacade.me$;
  users$ = this.facade.users$;

  criteria$ = this.appFacade.queryParams$.pipe(
    map((params: Params) =>
      ['search', 'limit', 'offset']
        .filter((f) => params[f] !== undefined && params[f] !== '')
        .reduce((acc, curr) => ({ ...acc, [curr]: params[curr] }), {})
    ),
    distinctUntilChanged(
      (a: UserCriteria, b: UserCriteria) =>
        a.search === b.search && a.limit === b.limit && a.offset === b.offset
    )
  );

  readonly Feature = Feature;

  constructor(
    private readonly appFacade: AppFacade,
    private readonly facade: UsersFacade,
    private readonly router: Router
  ) {}

  ngOnInit() {
    this.criteria$.pipe(takeUntil(this.facade.unload$)).subscribe((criteria: UserCriteria) =>
      this.facade.loadUsers({
        limit: 8,
        order: 'whenUpdated desc',
        ...criteria
      })
    );
  }

  ngOnDestroy(): void {
    this.facade.unload();
  }

  onFilter(criteria: FilterCriteria): Promise<boolean> {
    return navigate(this.router, criteria);
  }
}
