import {Component, OnInit} from '@angular/core';
import {AppFacade} from "../../../../+state/app.facade";
import {debounceTime, distinctUntilChanged, map, scan, shareReplay, startWith, take, tap} from "rxjs/operators";
import {RequestCriteria} from "@dev/translatr-sdk";
import {merge, Subject} from "rxjs";

@Component({
  selector: 'dev-dashboard-projects',
  templateUrl: './dashboard-projects.component.html',
  styleUrls: ['./dashboard-projects.component.css']
})
export class DashboardProjectsComponent {

  displayedColumns = ['name', 'owner', 'when_created', 'actions'];

  projects$ = this.facade.projects$;
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
    this.commands$.subscribe((criteria: RequestCriteria) => this.facade.loadProjects(criteria));
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
