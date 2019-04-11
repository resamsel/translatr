import {Component, OnInit} from '@angular/core';
import {AppFacade} from "../../../../+state/app.facade";

@Component({
  selector: 'dev-dashboard-projects',
  templateUrl: './dashboard-projects.component.html',
  styleUrls: ['./dashboard-projects.component.css']
})
export class DashboardProjectsComponent {

  projects$ = this.facade.projects$;
  displayedColumns = ['name', 'owner', 'when_created', 'actions'];

  constructor(private readonly facade: AppFacade) {
    facade.loadProjects();
  }
}
