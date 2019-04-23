import {Component} from '@angular/core';
import {Observable, of} from "rxjs";
import {AccessToken, RequestCriteria} from "@dev/translatr-model";
import {AppFacade} from "../../../../+state/app.facade";
import {Entity} from "@dev/translatr-components";

@Component({
  selector: 'dev-dashboard-access-tokens',
  templateUrl: './dashboard-access-tokens.component.html',
  styleUrls: ['./dashboard-access-tokens.component.css']
})
export class DashboardAccessTokensComponent {

  displayedColumns = ['name', 'user', 'scopes', 'when_created', 'actions'];

  me$ = this.facade.me$;
  accessTokens$ = this.facade.accessTokens$;
  load$ = of({limit: '20', order: 'name asc'});

  selected: Entity[] = [];

  constructor(private readonly facade: AppFacade) {
  }

  onSelected(entities: Entity[]) {
    this.selected = entities;
  }

  onCriteriaChanged(criteria: RequestCriteria) {
    this.facade.loadAccessTokens(criteria);
  }

  allowEdit$(accessToken: AccessToken): Observable<boolean> {
    return of(false); // this.me$.pipe(hasDeleteAccessTokenPermission(accessToken));
  }

  onEdit(accessToken: AccessToken) {
    // this.facade.deleteAccessToken(accessToken);
  }

  allowDelete$(accessToken: AccessToken): Observable<boolean> {
    return of(false); // this.me$.pipe(hasDeleteAccessTokenPermission(accessToken));
  }

  onDelete(accessToken: AccessToken) {
    // this.facade.deleteAccessToken(accessToken);
  }

  allowDeleteAll$(accessTokens: AccessToken[]): Observable<boolean> {
    return of(false); // this.me$.pipe(hasDeleteAllAccessTokensPermission(accessTokens));
  }
}
