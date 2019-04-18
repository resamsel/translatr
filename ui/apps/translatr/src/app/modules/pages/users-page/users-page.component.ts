import {Component, OnDestroy, OnInit} from '@angular/core';
import {PagedList, User} from "@dev/translatr-model";
import {Observable} from "rxjs";
import {UsersFacade} from "./+state/users.facade";
import {AppFacade} from "../../../+state/app.facade";

@Component({
  selector: 'app-projects-page',
  templateUrl: './users-page.component.html',
  styleUrls: ['./users-page.component.scss']
})
export class UsersPageComponent implements OnInit, OnDestroy {

  me$ = this.appFacade.me$;
  users$ = this.facade.users$;
  topContributors$: Observable<PagedList<User>>;

  constructor(private readonly appFacade: AppFacade, private readonly facade: UsersFacade) {
  }

  ngOnInit() {
    this.onLoadUsers(20);
  }

  ngOnDestroy(): void {
    this.facade.unload();
  }

  onLoadUsers(limit: number) {
    this.facade.loadUsers({order: 'whenUpdated desc', limit: `${limit}`});
  }
}
