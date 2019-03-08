import {Component, OnInit} from '@angular/core';
import {Project} from "../../../../shared/project";
import {ActivatedRoute} from "@angular/router";

@Component({
  selector: 'app-project-members',
  templateUrl: './project-members.component.html',
  styleUrls: ['./project-members.component.scss']
})
export class ProjectMembersComponent implements OnInit {

  project: Project;

  constructor(private readonly route: ActivatedRoute) {
  }

  ngOnInit() {
    this.route.parent.data
      .subscribe((data: { project: Project }) => {
        this.project = data.project;
      });
  }
}
