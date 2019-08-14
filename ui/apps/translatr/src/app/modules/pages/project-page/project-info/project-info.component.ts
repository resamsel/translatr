import { ChangeDetectionStrategy, Component } from '@angular/core';
import { Key, Locale, Message } from '@dev/translatr-model';
import { ProjectFacade } from '../+state/project.facade';
import { filter, map, pluck, switchMapTo } from 'rxjs/operators';
import { EMPTY, Observable } from 'rxjs';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-project-info',
  templateUrl: './project-info.component.html',
  styleUrls: ['./project-info.component.scss']
})
export class ProjectInfoComponent implements OnInit {
  project$ = this.facade.project$;
  activity$ = this.facade.activityAggregated$;
  locales$ = this.facade.locales$;
  latestLocales$ = this.locales$.pipe(
    filter(pagedList => !!pagedList && !!pagedList.list),
    pluck('list'),
    map((locales: Locale[]) => {
      return locales
        .slice()
        .sort(
          (a: Locale, b: Locale) =>
            b.whenUpdated.getTime() - a.whenUpdated.getTime()
        )
        .slice(0, 3);
    })
  );
  keys$ = this.facade.keys$;
  latestKeys$ = this.keys$.pipe(
    filter(pagedList => !!pagedList && !!pagedList.list),
    pluck('list'),
    map((keys: Key[]) => {
      console.log('keys', keys);
      return keys
        .slice()
        .sort(
          (a: Key, b: Key) => b.whenUpdated.getTime() - a.whenUpdated.getTime()
        )
        .slice(0, 3);
    })
  );
  latestMessages$: Observable<Message[]> = this.project$.pipe(switchMapTo(EMPTY));

  constructor(private readonly facade: ProjectFacade) {}

  ngOnInit() {
    this.project$
      .pipe(filter((project?: Project) => !!project && !!project.id))
      .subscribe((project: Project) => {
        this.facade.loadActivityAggregated(project.id);
      });
  }
}
