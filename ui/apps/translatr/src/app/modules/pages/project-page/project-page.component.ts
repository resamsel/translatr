import { Component, OnDestroy, OnInit } from '@angular/core';
import { Project } from "../../../shared/project";
import { ActivatedRoute, ParamMap } from "@angular/router";
import { ProjectFacade } from "./+state/project.facade";
import { Observable } from "rxjs";

@Component({
  selector: 'app-project-page',
  templateUrl: './project-page.component.html',
  styleUrls: ['./project-page.component.scss']
})
export class ProjectPageComponent implements OnInit, OnDestroy {

  project$ = this.projectFacade.project$;

  constructor(private route: ActivatedRoute, private readonly projectFacade: ProjectFacade) {
  }

  ngOnInit(): void {
    this.route.paramMap.subscribe((params: ParamMap) => {
      this.projectFacade.loadProject(params.get('username'), params.get('projectName'));
    });
  }

  ngOnDestroy(): void {
    this.projectFacade.unloadProject();
  }
}
