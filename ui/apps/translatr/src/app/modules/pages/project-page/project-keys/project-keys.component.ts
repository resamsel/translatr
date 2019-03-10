import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { ProjectFacade } from "../+state/project.facade";

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-project-keys',
  templateUrl: './project-keys.component.html',
  styleUrls: ['./project-keys.component.scss']
})
export class ProjectKeysComponent implements OnInit {

  project$ = this.projectFacade.project$;

  constructor(private readonly projectFacade: ProjectFacade) {
  }

  ngOnInit() {
  }
}
