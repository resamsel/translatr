import { Component, OnDestroy, OnInit } from '@angular/core';
import { ProjectsFacade } from './+state/projects.facade';
import { AppFacade } from '../../../+state/app.facade';
import { openProjectEditDialog } from '../../shared/project-edit-dialog/project-edit-dialog.component';
import { MatDialog } from '@angular/material/dialog';
import { filter, take } from 'rxjs/operators';
import { Router } from '@angular/router';

@Component({
  selector: 'app-projects-page',
  templateUrl: './projects-page.component.html',
  styleUrls: ['./projects-page.component.scss']
})
export class ProjectsPageComponent implements OnInit, OnDestroy {
  me$ = this.appFacade.me$;
  projects$ = this.facade.allProjects$;

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
    this.facade.loadProjects({ order: 'whenUpdated desc', limit });
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
