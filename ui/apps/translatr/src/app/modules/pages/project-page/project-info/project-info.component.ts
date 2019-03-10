import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { Project } from "../../../../shared/project";
import { Locale } from "../../../../shared/locale";
import { Key } from "../../../../shared/key";
import { ProjectFacade } from "../+state/project.facade";
import { filter, takeUntil } from "rxjs/operators";

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-project-info',
  templateUrl: './project-info.component.html',
  styleUrls: ['./project-info.component.scss']
})
export class ProjectInfoComponent implements OnInit {

  project$ = this.projectFacade.project$;
  activity$ = this.projectFacade.activityAggregated$;

  constructor(
    private readonly projectFacade: ProjectFacade) {
  }

  ngOnInit() {
    this.project$
      .pipe(
        takeUntil(this.projectFacade.unload$),
        filter((project?: Project) => project !== undefined)
      )
      .subscribe((project: Project) => {
        this.projectFacade.loadActivityAggregated(project.id);
      });
  }

  latestLocales(project?: Project): Array<Locale> {
    if (!project || !project.locales) {
      return [];
    }

    const locales = project.locales;

    return locales
      .sort((a: Locale, b: Locale) => b.whenUpdated.getTime() - a.whenUpdated.getTime())
      .slice(0, 3);
  }

  latestKeys(project?: Project): Array<Key> {
    if (!project || !project.keys) {
      return [];
    }

    const keys = project.keys;

    return keys
      .sort((a: Key, b: Key) => b.whenUpdated.getTime() - a.whenUpdated.getTime())
      .slice(0, 3);
  }
}
