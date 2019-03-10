import { Component, OnInit } from '@angular/core';
import { Project } from "../../../../shared/project";
import { ActivityCriteria } from "../../../../services/activity.service";
import { ProjectFacade } from "../+state/project.facade";
import { filter, takeUntil } from "rxjs/operators";

@Component({
  selector: 'app-project-activity',
  templateUrl: './project-activity.component.html',
  styleUrls: ['./project-activity.component.scss']
})
export class ProjectActivityComponent implements OnInit {

  project$ = this.projectFacade.project$;
  activities$ = this.projectFacade.activities$;
  private criteria: ActivityCriteria;

  constructor(private readonly projectFacade: ProjectFacade) {
  }

  ngOnInit() {
    this.project$
      .pipe(
        takeUntil(this.projectFacade.unload$),
        filter((project?: Project) => project !== undefined)
      )
      .subscribe((project: Project) => {
        this.criteria = {
          projectId: project.id,
          limit: 10
        };
        this.loadActivities();
      });
  }

  private loadActivities(): void {
    this.projectFacade.loadActivities(this.criteria);
  }

  onLoadMore(limit: number): void {
    this.criteria = {...this.criteria, limit};
    this.loadActivities();
  }
}
