import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PagedList, Project, ProjectCriteria, User } from '@dev/translatr-model';
import { ProjectService } from '@dev/translatr-sdk';
import { Observable } from 'rxjs';
import { openProjectEditDialog } from '../../../shared/project-edit-dialog/project-edit-dialog.component';
import { filter, take } from 'rxjs/operators';
import { MatDialog } from '@angular/material/dialog';

@Component({
  selector: 'app-user-projects',
  templateUrl: './user-projects.component.html',
  styleUrls: ['./user-projects.component.scss']
})
export class UserProjectsComponent implements OnInit {
  user: User;
  projects$: Observable<PagedList<Project>>;
  criteria: ProjectCriteria = {
    order: 'whenUpdated desc',
    limit: 10
  };

  constructor(
    private readonly projectService: ProjectService,
    private readonly router: Router,
    private readonly route: ActivatedRoute,
    private readonly dialog: MatDialog
  ) {
  }

  ngOnInit() {
    this.route.parent.data.subscribe((data: { user: User }) => {
      this.criteria.owner = data.user.username;
      this.loadProjects();
    });
  }

  onFilter(search: string) {
    if (!!search) {
      this.criteria.search = search;
    } else {
      delete this.criteria.search;
    }

    this.loadProjects();
  }

  openProjectCreationDialog(): void {
    openProjectEditDialog(this.dialog, {})
      .afterClosed()
      .pipe(
        take(1),
        filter(project => !!project)
      )
      .subscribe((project => this.router
        .navigate([project.ownerUsername, project.name])));
  }

  onMore(limit: number) {
    this.criteria.limit = limit;
    this.loadProjects();
  }

  private loadProjects(): void {
    this.projects$ = this.projectService.find(this.criteria);
  }
}
