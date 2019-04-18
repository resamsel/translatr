import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {User} from "../../../../../../../../libs/translatr-model/src/lib/model/user";
import {ProjectService} from "../../../../../../../../libs/translatr-sdk/src/lib/services/project.service";
import {Observable} from "rxjs";
import {PagedList} from "../../../../../../../../libs/translatr-model/src/lib/model/paged-list";
import {Project} from "../../../../../../../../libs/translatr-model/src/lib/model/project";
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
      owner: this.user.username,
      order: 'whenUpdated desc'
    });
  }

  openProjectCreationDialog(): void {
    const ref = this.dialog.open(ProjectCreationDialogComponent);
    ref.afterClosed()
      .pipe(take(1))
      .subscribe(() => this.loadProjects());
  }
}
