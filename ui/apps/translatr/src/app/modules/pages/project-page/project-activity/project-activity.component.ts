import { Component, OnInit } from '@angular/core';
import { Project } from "../../../../shared/project";
import { ActivatedRoute } from "@angular/router";
import { ProjectService } from "../../../../services/project.service";
import {ActivityCriteria, ActivityService} from "../../../../services/activity.service";

@Component({
  selector: 'app-project-activity',
  templateUrl: './project-activity.component.html',
  styleUrls: ['./project-activity.component.scss']
})
export class ProjectActivityComponent implements OnInit {

  project: Project;
  activities$;
  private criteria: ActivityCriteria;

  constructor(
    private readonly projectService: ProjectService,
    private readonly activityService: ActivityService,
    private readonly route: ActivatedRoute
  ) {
  }

  ngOnInit() {
    this.route.parent.data
      .subscribe((data: { project: Project }) => {
        this.project = data.project;
        this.criteria = {
          projectId: this.project.id,
          limit: 10
        };
        this.loadActivities();
      });
  }

  private loadActivities(): void {
    this.activities$ = this.activityService.activityList(this.criteria);
  }

  onLoadMore(limit: number): void {
    this.criteria = {...this.criteria, limit};
    this.loadActivities();
  }
}
