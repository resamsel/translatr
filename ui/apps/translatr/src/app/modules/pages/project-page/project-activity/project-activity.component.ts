import { Component, OnInit } from '@angular/core';
import { ActivityCriteria, Project } from '@dev/translatr-model';
import { ProjectFacade } from '../+state/project.facade';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-project-activity',
  templateUrl: './project-activity.component.html',
  styleUrls: ['./project-activity.component.scss']
})
export class ProjectActivityComponent implements OnInit {
  project$ = this.facade.project$;
  activities$ = this.facade.activities$;
  activity$ = this.facade.activityAggregated$;
  private criteria: ActivityCriteria;

  constructor(private readonly facade: ProjectFacade) {
  }

  ngOnInit() {
    this.project$
      .pipe(filter((project?: Project) => project !== undefined))
      .subscribe((project: Project) => {
        this.criteria = {
          projectId: project.id,
          limit: 10
        };
        this.loadActivities();
      });
  }

  private loadActivities(): void {
    this.facade.loadActivities(this.criteria);
  }

  onMore(): void {
    this.criteria = { ...this.criteria, limit: this.criteria.limit * 2 };
    this.loadActivities();
  }

  onFilter(search: string) {
    if (!!search) {
      this.criteria = { ...this.criteria, search };
    } else {
      delete this.criteria.search;
    }

    this.loadActivities();
  }
}
