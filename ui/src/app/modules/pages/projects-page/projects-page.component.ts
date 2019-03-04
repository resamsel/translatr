import {Component, OnInit} from '@angular/core';
import {Observable} from "rxjs";
import {PagedList} from "../../../shared/paged-list";
import {Project} from "../../../shared/project";
import {ProjectService} from "../../../services/project.service";

@Component({
  selector: 'app-projects-page',
  templateUrl: './projects-page.component.html'
})
export class ProjectsPageComponent implements OnInit {
  projects$: Observable<PagedList<Project>>;

  constructor(private readonly projectService: ProjectService) {
  }

  ngOnInit(): void {
    this.projects$ = this.projectService.getProjects();
  }
}
