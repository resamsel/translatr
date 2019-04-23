import { Component } from '@angular/core';
import { merge, Observable, Subject } from "rxjs";
import { debounceTime, distinctUntilChanged, map, scan, shareReplay, startWith, take } from "rxjs/operators";
import { AccessToken, RequestCriteria } from "@dev/translatr-model";
import { AppFacade } from "../../../../+state/app.facade";
import { of } from "rxjs/internal/observable/of";
import { Entity } from "@dev/translatr-components";

@Component({
  selector: 'dev-dashboard-access-tokens',
  templateUrl: './dashboard-access-tokens.component.html',
  styleUrls: ['./dashboard-access-tokens.component.css']
})
export class DashboardAccessTokensComponent {

  displayedColumns = ['name', 'user', 'scopes', 'when_created', 'actions'];

  me$ = this.facade.me$;
  accessTokens$ = this.facade.accessTokens$;
  search$ = new Subject<string>();
  limit$ = new Subject<number>();
  commands$ = merge(
    this.search$.asObservable().pipe(
      distinctUntilChanged(),
      debounceTime(200),
      map((search: string) => ({search}))
    ),
    this.limit$.asObservable().pipe(
      distinctUntilChanged(),
      map((limit: number) => ({limit: `${limit}`}))
    )
  )
    .pipe(
      startWith({limit: '20', search: '', order: 'name asc'}),
      scan((acc: RequestCriteria, value: RequestCriteria) => ({...acc, ...value})),
      shareReplay(1)
    );

  selected: Entity[] = [];

  constructor(private readonly facade: AppFacade) {
    this.commands$.subscribe((criteria: RequestCriteria) => this.facade.loadAccessTokens(criteria));
  }

  onSelected(entities: Entity[]) {
    this.selected = entities;
  }

  onFilter(value: string) {
    this.search$.next(value);
  }

  onLoadMore() {
    this.commands$
      .pipe(take(1))
      .subscribe((criteria: RequestCriteria) =>
        this.limit$.next(parseInt(criteria.limit, 10) * 2));
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
