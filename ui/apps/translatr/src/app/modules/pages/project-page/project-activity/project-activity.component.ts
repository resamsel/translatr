import { Component, OnInit } from '@angular/core';
import { Project } from '@dev/translatr-model';
import { ActivityCriteria } from '@dev/translatr-sdk';
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
  private criteria: ActivityCriteria;

  constructor(private readonly facade: ProjectFacade) {}

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
}
