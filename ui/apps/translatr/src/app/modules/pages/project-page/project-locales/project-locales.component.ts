import { Component, OnInit } from '@angular/core';
import { ProjectFacade } from "../+state/project.facade";
import { take, tap } from "rxjs/operators";
import { Project } from "../../../../../../../../libs/translatr-sdk/src/lib/shared/project";
import { Locale } from "../../../../../../../../libs/translatr-sdk/src/lib/shared/locale";
import { MatSnackBar } from "@angular/material";

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

  constructor(
    private readonly facade: ProjectFacade,
    private readonly snackBar: MatSnackBar
  ) {
  }

  ngOnInit() {
  }

  onMore(limit: number) {
    this.facade.project$
      .pipe(take(1))
      .subscribe((project: Project) =>
        this.facade.loadLocales(project.id, {limit: `${limit}`}));
  }

  onEdit(locale: Locale) {
    this.snackBar.open(`Edit locale ${locale.displayName}`, 'Dismiss', {duration: 2000});
  }

  onDelete(locale: Locale) {
    this.snackBar.open(`Delete locale ${locale.displayName}`, 'Undo', {duration: 2000});
  }
}
