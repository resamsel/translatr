import { Component, OnInit } from '@angular/core';
import { Project } from "../../../../shared/project";
import { ActivatedRoute } from "@angular/router";
import { ProjectService } from "../../../../services/project.service";

@Component({
  selector: 'app-project-activity',
  templateUrl: './project-activity.component.html',
  styleUrls: ['./project-activity.component.scss']
})
export class ProjectActivityComponent implements OnInit {

  project: Project;
  activity$;
  activityList$;

  constructor(
    private readonly projectService: ProjectService,
    private readonly route: ActivatedRoute
  ) {
  }

  ngOnInit() {
    this.route.parent.data
      .subscribe((data: { project: Project }) => {
        this.project = data.project;
        this.activity$ = this.projectService.activity(this.project.id);
        this.activityList$ = this.projectService.activityList(this.project.id);
      });
  }
}
