import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { ProjectFacade } from "../+state/project.facade";
import { take, tap } from "rxjs/operators";
import { Project } from "../../../../shared/project";

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-project-keys',
  templateUrl: './project-keys.component.html',
  styleUrls: ['./project-keys.component.scss']
})
export class ProjectKeysComponent implements OnInit {

  project$ = this.facade.project$.pipe(
    tap((project: Project) => {
      if (!!project) {
        this.facade.loadKeys(project.id)
      }}));
  keys$ = this.facade.keys$;

  constructor(private readonly facade: ProjectFacade) {
  }

  ngOnInit() {
  }

  onMore(limit: number) {
    this.facade.project$
      .pipe(take(1))
      .subscribe((project: Project) =>
        this.facade.loadKeys(project.id, {limit: `${limit}`}));
  }
}
