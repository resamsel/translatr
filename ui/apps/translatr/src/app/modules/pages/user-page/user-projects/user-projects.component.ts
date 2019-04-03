import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {User} from "../../../../shared/user";
import {ProjectService} from "../../../../services/project.service";
import {Observable} from "rxjs";
import {PagedList} from "../../../../shared/paged-list";
import {Project} from "../../../../shared/project";
import {ProjectCreationDialogComponent} from "../../../shared/project-creation-dialog/project-creation-dialog.component";
import {take} from "rxjs/operators";
import {MatDialog} from "@angular/material";

@Component({
  selector: 'app-user-projects',
  templateUrl: './user-projects.component.html',
  styleUrls: ['./user-projects.component.scss']
})
export class UserProjectsComponent implements OnInit {

  user: User;
  projects$: Observable<PagedList<Project>>;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly projectService: ProjectService,
    private readonly dialog: MatDialog
  ) {
  }

  ngOnInit() {
    this.route.parent.data
      .subscribe((data: { user: User }) => {
        this.user = data.user;
        this.loadProjects();
      });
  }

  private loadProjects(): void {
    this.projects$ = this.projectService.getProjects({
      params: {
        owner: this.user.username,
        order: 'whenUpdated desc'
      }
    });
  }

  openProjectCreationDialog(): void {
    const ref = this.dialog.open(ProjectCreationDialogComponent);
    ref.afterClosed()
      .pipe(take(1))
      .subscribe(() => this.loadProjects());
  }
}
