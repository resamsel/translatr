import { Component, Inject, Injector, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, CanActivate, Route } from '@angular/router';
import { ProjectFacade } from './+state/project.facade';
import { distinctUntilChanged, filter, pluck, takeUntil, tap } from 'rxjs/operators';
import { AppFacade } from '../../../+state/app.facade';
import { canActivate$, NameIconRoute } from '@translatr/utils';
import { PROJECT_ROUTES } from './project-page.token';
import { Observable } from 'rxjs';
import { Project } from '@dev/translatr-model';

@Component({
  selector: 'app-project-page',
  templateUrl: './project-page.component.html',
  styleUrls: ['./project-page.component.scss']
})
export class ProjectPageComponent implements OnInit, OnDestroy {
  me$ = this.appFacade.me$;
  project$ = this.facade.project$.pipe(
    filter(project => !!project)
  );

  children: NameIconRoute[] = this.routes[0].children;

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
      pluck('id'),
      distinctUntilChanged(),
      tap((projectId: string) => {
        this.facade.loadLocales(projectId, {});
        this.facade.loadKeys(projectId, {});
        this.facade.loadMessages(projectId, { order: 'whenCreated desc' });
        this.facade.loadActivityAggregated(projectId);
      }),
      takeUntil(this.facade.unload$)
    ).subscribe();
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
