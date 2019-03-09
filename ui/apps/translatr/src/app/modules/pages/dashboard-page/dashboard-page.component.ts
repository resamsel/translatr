import { Component, OnInit } from '@angular/core';
import { Observable } from "rxjs";
import { PagedList } from "../../../shared/paged-list";
import { Project } from "../../../shared/project";
import { ProjectService } from "../../../services/project.service";
import { MatDialog } from "@angular/material";

@Component({
  selector: 'app-projects-page',
  templateUrl: './dashboard-page.component.html'
})
export class DashboardPageComponent implements OnInit {
  projects$: Observable<PagedList<Project>>;

  constructor(private readonly projectService: ProjectService, private readonly dialog: MatDialog) {
  }

  ngOnInit(): void {
    this.loadProjects();
  }

  loadProjects(): void {
    this.projects$ = this.projectService.getProjects();
  }
}
