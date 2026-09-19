import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { Params, Router } from '@angular/router';
import { FeatureFlagClassDirective } from '@dev/translatr-components';
import { Feature, UserCriteria } from '@dev/translatr-model';
import { TranslocoModule } from '@jsverse/transloco';
import { navigate } from '@translatr/utils';
import { distinctUntilChanged, map, takeUntil } from 'rxjs/operators';
import { AppFacade } from '../../../+state/app.facade';
import { SidenavComponent } from '../../nav/sidenav/sidenav.component';
import { FilterCriteria } from '../../shared/list-header/list-header.component';
import { UserListComponent } from '../../shared/user-list/user-list.component';
import { UsersFacade } from './+state/users.facade';

@Component({
  standalone: true,
  selector: 'app-projects-page',
  templateUrl: './users-page.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./users-page.component.scss'],
  imports: [CommonModule, TranslocoModule, SidenavComponent, UserListComponent, FeatureFlagClassDirective]
})
export class UsersPageComponent implements OnInit, OnDestroy {
  me$ = this.appFacade.me$;
  users$ = this.facade.users$;

  criteria$ = this.appFacade.queryParams$.pipe(
    map((params: Params) =>
      ['search', 'limit', 'offset']
        .filter(f => params[f] !== undefined && params[f] !== '')
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
