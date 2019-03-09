import { Component, OnInit } from '@angular/core';
import { Project } from "../../../shared/project";
import { ActivatedRoute } from "@angular/router";
import { PagedList } from "../../../shared/paged-list";

@Component({
  selector: 'app-projects-page',
  templateUrl: './projects-page.component.html',
  styleUrls: ['./projects-page.component.scss']
})
export class ProjectsPageComponent implements OnInit {

  projects: PagedList<Project>;

  constructor(private readonly route: ActivatedRoute) {
  }

  ngOnInit() {
    this.route.data
      .subscribe((data: { projects: PagedList<Project> }) => {
        this.projects = data.projects;
      });
  }
}
