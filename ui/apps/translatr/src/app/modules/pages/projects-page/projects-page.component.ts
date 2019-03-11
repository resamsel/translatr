import {Component, OnDestroy, OnInit} from '@angular/core';
import {ProjectsFacade} from "./+state/projects.facade";
import {takeUntil} from "rxjs/operators";
import {Observable, Subject} from "rxjs";
import {Project} from "../../../shared/project";
import {PagedList} from "../../../shared/paged-list";

@Component({
  selector: 'app-projects-page',
  templateUrl: './projects-page.component.html',
  styleUrls: ['./projects-page.component.scss']
})
export class ProjectsPageComponent implements OnInit, OnDestroy {
  public projects$ = this.projectsFacade.allProjects$;

  constructor(private readonly projectsFacade: ProjectsFacade) {
  }

  ngOnInit() {
    this.projectsFacade.loadProjects();
  }

  ngOnDestroy(): void {
    this.projectsFacade.unloadProjects();
  }
}
