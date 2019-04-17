import {Component} from '@angular/core';
import {AppFacade} from "../../../../+state/app.facade";
import {debounceTime, distinctUntilChanged, map, mapTo, scan, shareReplay, startWith, take, tap} from "rxjs/operators";
import {Project, RequestCriteria, User} from "@dev/translatr-sdk";
import {merge, Observable, Subject} from "rxjs";
import {isAdmin} from "../dashboard-users/dashboard-users.component";
import {ProjectDeleted, ProjectDeleteError} from "../../../../+state/app.actions";

const hasEditProjectPermission = (project: Project) => map((me?: User) =>
  (me !== undefined && me.id === project.ownerId) || isAdmin(me));

const hasDeleteProjectPermission = (project: Project) => map((me?: User) =>
  (me !== undefined && me.id === project.ownerId) || isAdmin(me));

@Component({
  selector: 'dev-dashboard-projects',
  templateUrl: './dashboard-projects.component.html',
  styleUrls: ['./dashboard-projects.component.css']
})
export class DashboardProjectsComponent {

  displayedColumns = ['name', 'description', 'owner', 'when_created', 'actions'];

  me$ = this.facade.me$;
  projects$ = this.facade.projects$;
  projectDeleted$ = this.facade.projectDeleted$;
  search$ = new Subject<string>();
  limit$ = new Subject<number>();
  reload$ = new Subject<void>();
  commands$ = merge(
    this.search$.asObservable().pipe(
      distinctUntilChanged(),
      debounceTime(200),
      map((search: string) => ({search}))
    ),
    this.limit$.asObservable().pipe(
      distinctUntilChanged(),
      map((limit: number) => ({limit: `${limit}`}))
    ),
    this.reload$.asObservable().pipe(mapTo({}))
  )
    .pipe(
      startWith({limit: '20', search: '', order: 'name asc'}),
      scan((acc: RequestCriteria, value: RequestCriteria) => ({...acc, ...value})),
      shareReplay(1)
    );

  constructor(private readonly facade: AppFacade) {
    this.commands$.subscribe((criteria: RequestCriteria) => this.facade.loadProjects(criteria));
  }

  trackByFn(index: number, item: { id: string }): string {
    return item.id;
  }

  allowEdit$(project: Project): Observable<boolean> {
    return this.me$.pipe(hasEditProjectPermission(project));
  }

  allowDelete$(project: Project): Observable<boolean> {
    return this.me$.pipe(hasDeleteProjectPermission(project));
  }

  onDelete(project: Project) {
    this.projectDeleted$
      .pipe(take(1))
      .subscribe((action: ProjectDeleted | ProjectDeleteError) => {
        if (action instanceof ProjectDeleted) {
          console.log(`Project ${action.payload.name} has been deleted`);
          this.reload$.next();
        } else {
          console.warn(`Project ${action.payload.error.error} could not be deleted`);
        }
      });
    console.log('deleting...');
    this.facade.deleteProject(project);
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
