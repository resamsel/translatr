import { CommonModule } from '@angular/common';
import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { ActivityGraphComponent } from '@dev/translatr-components';
import { filter, map,  takeUntil, withLatestFrom } from 'rxjs/operators';
import { ActivityListComponent } from '../../../shared/activity-list/activity-list.component';
import { ProjectFacade } from '../../../shared/project-state';

@Component({
  standalone: true,
  selector: 'app-project-activity',
  templateUrl: './project-activity.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./project-activity.component.scss'],
  imports: [CommonModule, ActivityGraphComponent, ActivityListComponent]
})
export class ProjectActivityComponent implements OnInit {
  project$ = this.facade.project$;
  activities$ = this.facade.activities$;
  aggregated$ = this.facade.activityAggregated$.pipe(
    map(pagedList => (pagedList ? pagedList.list : []))
  );
  criteria$ = this.facade.activitiesCriteria$;

  constructor(private readonly facade: ProjectFacade) {}

  ngOnInit() {
    this.criteria$
      .pipe(
        withLatestFrom(
          this.project$.pipe(
            filter(x => !!x),
            map((v: any) => v['id'])
          )
        ),
        takeUntil(this.facade.unload$)
      )
      .subscribe(([criteria, projectId]) => {
        this.facade.loadActivities(projectId, criteria);
      });
  }
}
