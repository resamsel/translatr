import { ChangeDetectionStrategy, Component, Inject, Injector, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, CanActivate, Route } from '@angular/router';
import { ProjectFacade } from './+state/project.facade';
import { distinctUntilKeyChanged, filter, map, scan, take, tap } from 'rxjs/operators';
import { AppFacade } from '../../../+state/app.facade';
import { canActivate$, NameIconRoute } from '@translatr/utils';
import { PROJECT_ROUTES } from './project-page.token';
import { merge, Observable } from 'rxjs';
import { Project } from '@dev/translatr-model';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-project-page',
  templateUrl: './project-page.component.html',
  styleUrls: ['./project-page.component.scss']
})
export class ProjectPageComponent implements OnInit, OnDestroy {
  me$ = this.appFacade.me$;
  // TODO: put this logic somewhere else, i.e. each page should only load
  //  what it needs, but cache it for other usages
  project$ = this.facade.project$.pipe(
    filter(project => !!project)
  );

  children: NameIconRoute[] = this.routes[0].children;
  childrenActive$ = merge(...this.children.map(child => this.canActivate$(child).pipe(
    take(1),
    map((can) => ({ [child.path]: can }))
  )))
    .pipe(
      scan((acc, curr) => ({ ...acc, ...curr }), {})
    );

  constructor(
    private readonly injector: Injector,
    private readonly route: ActivatedRoute,
    private readonly facade: ProjectFacade,
    private readonly appFacade: AppFacade,
    @Inject(PROJECT_ROUTES) private routes: { children: NameIconRoute[] }[]
  ) {
  }

  ngOnInit(): void {
    this.project$.pipe(
      distinctUntilKeyChanged('id'),
      tap((project: Project) => {
        console.log('project', project);
        this.facade.loadLocales(project.id, {});
        this.facade.loadKeys(project.id, {});
        this.facade.loadMembers(project.id, {});
        this.facade.loadMessages(project.id, { order: 'whenCreated desc' });
        this.facade.loadActivityAggregated(project.id);
        this.facade.loadActivities(project.id);
      }))
      .subscribe();
  }

  ngOnDestroy(): void {
    this.facade.unloadProject();
  }

  routerLink(project: Project | undefined, route: Route): string | undefined {
    if (project === undefined) {
      return undefined;
    }

    if (route === '') {
      return `/${project.ownerUsername}/${project.name}`;
    }

    return `/${project.ownerUsername}/${project.name}/${route.path}`;
  }

  canActivate$(route: NameIconRoute): Observable<boolean> {
    return canActivate$(
      route,
      this.route,
      (guard: any) => this.injector.get<CanActivate>(guard)
    );
  }
}
