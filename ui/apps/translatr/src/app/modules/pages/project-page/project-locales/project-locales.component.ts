import { Component, OnInit } from '@angular/core';
import { ProjectFacade } from "../+state/project.facade";
import { take, tap } from "rxjs/operators";
import { Project } from "../../../../shared/project";

@Component({
  selector: 'app-project-locales',
  templateUrl: './project-locales.component.html',
  styleUrls: ['./project-locales.component.scss']
})
export class ProjectLocalesComponent implements OnInit {

  project$ = this.facade.project$.pipe(
    tap((project: Project) => {
      if (!!project) {
        this.facade.loadLocales(project.id)
      }
    }));
  locales$ = this.facade.locales$;

  constructor(private readonly facade: ProjectFacade) {
  }

  ngOnInit() {
  }

  onMore(limit: number) {
    this.facade.project$
      .pipe(take(1))
      .subscribe((project: Project) =>
        this.facade.loadLocales(project.id, {limit: `${limit}`}));
  }
}
