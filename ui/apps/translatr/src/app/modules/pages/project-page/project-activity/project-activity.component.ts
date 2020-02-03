import { Component, OnInit } from '@angular/core';
import { ProjectFacade } from '../+state/project.facade';
import { filter, pluck, takeUntil, withLatestFrom } from 'rxjs/operators';
import { Project } from '@dev/translatr-model';

@Component({
  selector: 'app-project-activity',
  templateUrl: './project-activity.component.html',
  styleUrls: ['./project-activity.component.scss']
})
export class ProjectActivityComponent implements OnInit {
  project$ = this.facade.project$;
  activities$ = this.facade.activities$;
  activity$ = this.facade.activityAggregated$;
  criteria$ = this.facade.activitiesCriteria$;

  constructor(private readonly facade: ProjectFacade) {
  }

  ngOnInit() {
    this.criteria$
      .pipe(
        withLatestFrom(this.project$.pipe(
          filter(x => !!x),
          pluck<Project, string>('id')
        )),
        takeUntil(this.facade.unload$)
      )
      .subscribe(([criteria, projectId]) => {
        this.facade.loadActivities(projectId, criteria);
      });
  }
}
