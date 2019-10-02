import { Component, OnDestroy, OnInit } from '@angular/core';
import { ProjectsFacade } from './+state/projects.facade';
import { AppFacade } from '../../../+state/app.facade';
import { openProjectEditDialog } from '../../shared/project-edit-dialog/project-edit-dialog.component';
import { MatDialog } from '@angular/material/dialog';
import { filter, map, take } from 'rxjs/operators';
import { Router } from '@angular/router';
import { PagedList, Project, User } from '@dev/translatr-model';

@Component({
  selector: 'app-projects-page',
  templateUrl: './projects-page.component.html',
  styleUrls: ['./projects-page.component.scss']
})
export class ProjectsPageComponent implements OnInit, OnDestroy {
  me$ = this.appFacade.me$;
  projects$ = this.facade.allProjects$;
  projectsTail$ = this.projects$.pipe(
    filter(pagedList => !!pagedList),
    map((pagedList: PagedList<Project>) => ({
      ...pagedList,
      list: pagedList.list.slice(4)
    }))
  );

  constructor(
    private readonly facade: ProjectsFacade,
    private readonly appFacade: AppFacade,
    private readonly dialog: MatDialog,
    private readonly router: Router
  ) {
  }

  ngOnInit() {
    this.onLoadProjects(20);
  }

  ngOnDestroy(): void {
    this.facade.unloadProjects();
  }

  onLoadProjects(limit: number) {
    this.me$.pipe(
      filter(user => !!user),
      take(1)
    ).subscribe((user: User) =>
      this.facade.loadProjects({
        memberId: user.id,
        order: 'whenUpdated desc',
        limit
      }));
  }

  openProjectCreationDialog() {
    openProjectEditDialog(this.dialog, {})
      .afterClosed()
      .pipe(
        take(1),
        filter(project => !!project)
      )
      .subscribe((project => this.router
        .navigate([project.ownerUsername, project.name])));
  }
}
