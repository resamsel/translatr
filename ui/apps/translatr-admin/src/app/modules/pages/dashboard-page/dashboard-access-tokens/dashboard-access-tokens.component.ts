import { Component, OnInit } from '@angular/core';
import {merge, Subject} from "rxjs";
import {debounceTime, distinctUntilChanged, map, scan, shareReplay, startWith, take, tap} from "rxjs/operators";
import {RequestCriteria} from "@dev/translatr-model";
import {AppFacade} from "../../../../+state/app.facade";

@Component({
  selector: 'dev-dashboard-access-tokens',
  templateUrl: './dashboard-access-tokens.component.html',
  styleUrls: ['./dashboard-access-tokens.component.css']
})
export class DashboardAccessTokensComponent {

  displayedColumns = ['name', 'user', 'when_created', 'actions'];

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

  constructor(private readonly facade: AppFacade) {
    this.commands$.subscribe((criteria: RequestCriteria) => this.facade.loadAccessTokens(criteria));
  }

  onFilter(value: string) {
    this.search$.next(value);
  }

  onLoadMore() {
    this.commands$
      .pipe(tap(console.log), take(1))
      .subscribe((criteria: RequestCriteria) =>
        this.limit$.next(parseInt(criteria.limit, 10) * 2));
  }
}
