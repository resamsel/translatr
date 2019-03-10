import { Component, OnInit } from '@angular/core';
import { ProjectFacade } from "../+state/project.facade";

@Component({
  selector: 'app-project-members',
  templateUrl: './project-members.component.html',
  styleUrls: ['./project-members.component.scss']
})
export class ProjectMembersComponent implements OnInit {

  project$ = this.projectFacade.project$;

  constructor(private readonly projectFacade: ProjectFacade) {
  }

  ngOnInit() {
  }
}
