import { ChangeDetectionStrategy, Component, HostBinding, Input } from '@angular/core';
import { Project } from '@dev/translatr-model';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-activity-project-link',
  templateUrl: './activity-project-link.component.html',
  styleUrls: ['./activity-project-link.component.scss']
})
export class ActivityProjectLinkComponent {
  projectLink: string[] | undefined;
  @HostBinding('class.sub-title') subtitle = true;

  private _project: Project;

  get project(): Project {
    return this._project;
  }

  @Input() set project(project: Project) {
    this._project = project;

    if (!project || !project.ownerUsername || !project.name) {
      this.projectLink = undefined;
    } else {
      this.projectLink = ['/', project.ownerUsername, project.name];
    }
  }
}
