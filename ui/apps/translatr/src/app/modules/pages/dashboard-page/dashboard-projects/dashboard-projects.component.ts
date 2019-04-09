import { Component, OnInit } from '@angular/core';
import { ProjectCreationDialogComponent } from "../../../shared/project-creation-dialog/project-creation-dialog.component";
import { take } from "rxjs/operators";
import { Project } from "../../../../../../../../libs/translatr-sdk/src/lib/shared/project";
import { PagedList } from "../../../../../../../../libs/translatr-sdk/src/lib/shared/paged-list";
import { ActivatedRoute } from "@angular/router";
import { MatDialog } from "@angular/material";

@Component({
  selector: 'app-dashboard-projects',
  templateUrl: './dashboard-projects.component.html',
  styleUrls: ['./dashboard-projects.component.scss']
})
export class DashboardProjectsComponent implements OnInit {
  projects: PagedList<Project>;

  constructor(private readonly route: ActivatedRoute, private readonly dialog: MatDialog) {
  }

  ngOnInit() {
    this.route.data
      .subscribe((data: { projects: PagedList<Project> }) => {
        this.projects = data.projects;
      });
  }

  openProjectCreationDialog(): void {
    const ref = this.dialog.open(ProjectCreationDialogComponent);
    ref.afterClosed()
      .pipe(take(1))
      .subscribe(() => this.loadProjects());
  }

  private loadProjects(): void {}
}
