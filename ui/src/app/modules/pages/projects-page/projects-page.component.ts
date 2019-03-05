import {Component, OnInit} from '@angular/core';
import {Observable} from "rxjs";
import {PagedList} from "../../../shared/paged-list";
import {Project} from "../../../shared/project";
import {ProjectService} from "../../../services/project.service";
import {ProjectCreationDialogComponent} from "../../shared/project-creation-dialog/project-creation-dialog.component";
import {MatDialog} from "@angular/material";
import {take} from "rxjs/operators";

@Component({
  selector: 'app-projects-page',
  templateUrl: './projects-page.component.html'
})
export class ProjectsPageComponent implements OnInit {
  projects$: Observable<PagedList<Project>>;

  constructor(private readonly projectService: ProjectService, private readonly dialog: MatDialog) {
  }

  ngOnInit(): void {
    this.loadProjects();
  }

  loadProjects(): void {
    this.projects$ = this.projectService.getProjects();
  }

  openProjectCreationDialog(): void {
    const ref = this.dialog.open(ProjectCreationDialogComponent);
    ref.afterClosed()
      .pipe(take(1))
      .subscribe(() => this.loadProjects());
  }
}
