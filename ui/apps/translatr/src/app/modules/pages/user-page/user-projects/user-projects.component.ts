import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { PagedList, Project, User } from '@dev/translatr-model';
import { ProjectService } from '@dev/translatr-sdk';
import { Observable } from 'rxjs';
import { ProjectCreationDialogComponent } from '../../../shared/project-creation-dialog/project-creation-dialog.component';
import { take } from 'rxjs/operators';
import { MatDialog } from '@angular/material';

@Component({
  selector: 'app-user-projects',
  templateUrl: './user-projects.component.html',
  styleUrls: ['./user-projects.component.scss']
})
export class UserProjectsComponent implements OnInit {
  user: User;
  projects$: Observable<PagedList<Project>>;

  constructor(
    private readonly projectService: ProjectService,
    private readonly route: ActivatedRoute,
    private readonly dialog: MatDialog
  ) {}

  ngOnInit() {
    this.route.parent.data.subscribe((data: { user: User }) => {
      this.user = data.user;
      this.loadProjects();
    });
  }

  private loadProjects(): void {
    this.projects$ = this.projectService.find({
      owner: this.user.username,
      order: 'whenUpdated desc',
      limit: '10'
    });
  }

  openProjectCreationDialog(): void {
    const ref = this.dialog.open(ProjectCreationDialogComponent);
    ref
      .afterClosed()
      .pipe(take(1))
      .subscribe(() => this.loadProjects());
  }
}
