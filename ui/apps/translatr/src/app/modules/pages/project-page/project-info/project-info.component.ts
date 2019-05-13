import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { Key, Locale, Project } from '@dev/translatr-model';
import { ProjectFacade } from '../+state/project.facade';
import { filter, map, pluck, switchMapTo } from 'rxjs/operators';
import { EMPTY } from 'rxjs';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-project-info',
  templateUrl: './project-info.component.html',
  styleUrls: ['./project-info.component.scss']
})
export class ProjectInfoComponent implements OnInit {
  project$ = this.facade.project$;
  activity$ = this.facade.activityAggregated$;
  latestLocales$ = this.project$.pipe(
    filter(project => !!project && !!project.locales),
    pluck<Project, Locale[]>('locales'),
    map((locales: Locale[]) => {
      return locales
        .sort(
          (a: Locale, b: Locale) =>
            b.whenUpdated.getTime() - a.whenUpdated.getTime()
        )
        .slice(0, 3);
    })
  );
  latestKeys$ = this.project$.pipe(
    filter(project => !!project && !!project.keys),
    pluck('keys'),
    map((keys: Key[]) => {
      return keys
        .sort(
          (a: Key, b: Key) => b.whenUpdated.getTime() - a.whenUpdated.getTime()
        )
        .slice(0, 3);
    })
  );
  latestMessages$ = this.project$.pipe(switchMapTo(EMPTY));

  constructor(private readonly facade: ProjectFacade) {}

  ngOnInit() {
    this.project$
      .pipe(filter((project?: Project) => !!project && !!project.id))
      .subscribe((project: Project) => {
        this.facade.loadActivityAggregated(project.id);
      });
  }
}
