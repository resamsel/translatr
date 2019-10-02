import { Component, OnDestroy, OnInit } from '@angular/core';
import { PagedList, User } from '@dev/translatr-model';
import { UsersFacade } from './+state/users.facade';
import { AppFacade } from '../../../+state/app.facade';
import { filter, map } from 'rxjs/operators';

@Component({
  selector: 'app-projects-page',
  templateUrl: './users-page.component.html',
  styleUrls: ['./users-page.component.scss']
})
export class UsersPageComponent implements OnInit, OnDestroy {
  me$ = this.appFacade.me$;
  users$ = this.facade.users$;
  usersTail$ = this.users$.pipe(
    filter(pagedList => !!pagedList),
    map((pagedList: PagedList<User>) => ({
      ...pagedList,
      list: pagedList.list.slice(4)
    }))
  );

  constructor(
    private readonly appFacade: AppFacade,
    private readonly facade: UsersFacade
  ) {
  }

  ngOnInit() {
    this.onLoadUsers(8);
  }

  ngOnDestroy(): void {
    this.facade.unload();
  }

  onLoadUsers(limit: number) {
    this.facade.loadUsers({ order: 'whenUpdated desc', limit });
  }
}
