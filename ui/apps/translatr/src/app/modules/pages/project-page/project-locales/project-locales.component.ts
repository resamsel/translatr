import { Component, OnInit } from '@angular/core';
import { ProjectFacade } from "../+state/project.facade";

@Component({
  selector: 'app-project-locales',
  templateUrl: './project-locales.component.html',
  styleUrls: ['./project-locales.component.scss']
})
export class ProjectLocalesComponent implements OnInit {

  project$ = this.projectFacade.project$;

  constructor(private readonly projectFacade: ProjectFacade) {
  }

  ngOnInit() {
  }
}
